package com.platform.match.service.utils;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author payphone
 * @date 2022-12-13
 * 完成锁粒度的细化，使用 temporary 暂存需要匹配的人，用 temporaryLock 控制添加用户的并发。新增用户不再直接新增到匹配池中。
 */
@Component
public class QuickMatchingPool extends MatchingPool {
    private static TreeSet<Player> players = new TreeSet<>(Comparator.comparingInt(Player::getRating));

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        QuickMatchingPool.restTemplate = restTemplate;
    }

    protected boolean playerWithMatching(Player people) {
        if (people.getBotId() == -100) {
            // 如果是人机对战，在分配一个机器人给他。
            // 人机对战
            Player machine = GenerateMachinePlayer.generateMachinePlayer();
            sendResult(people, machine);
            return players.remove(people);
        }
        return false;
    }

    @Override
    public void increaseWaitingTime() {
        for (Player player : players) {
            if (player.getWaitingTime() <= 19) {
                player.setWaitingTime(player.getWaitingTime() + 1);
            }
        }
    }

    public QuickMatchingPool() {
    }

    public QuickMatchingPool(MatcherRule rule) {
        this.matcherRule = rule;
    }

    @Override
    public void matchPlayers() {  // 尝试匹配所有玩家
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();

            // 如果需要被移除，则移除对象并不做任何匹配
            if (needRemove.containsKey(player.getUserId())) {
                // 两个线程安全方法的组合代码不一定线程安全，但是 containsKey 和 remove 都是一个线程中运行，因此无并发安全问题，
                // containsKey 和 put 方法无并发安全问题。最多是短暂的不一致性
                iterator.remove();
                // remove 和 put 方法如果操作同一个 key，则会争用同一把锁，因此也无线程安全问题。
                needRemove.remove(player.getUserId());
            }

            // 人机对战，则继续轮询下一个用户
            if (playerWithMatching(player)) continue;

            Player higher = players.higher(player);
            Player lower = players.lower(player);

            // 无合适的匹配对象
            if (higher == null && lower == null) continue;

            // 匹配
            if (higher != null && matcherRule.matcher(player, higher)) {
                sendResult(player, higher);
                System.out.println("匹配到高分段人选");
                iterator.remove();
                continue;
            }

            if (lower != null && matcherRule.matcher(player, lower)) {
                System.out.println("匹配到低分段人选");
                sendResult(player, lower);
                iterator.remove();
            }
        }

        if (temporary.size() == 0) return;
        // 添加新用户
        try {
            temporaryLock.lock();
            players.addAll(temporary);
            temporary.clear();
        } finally {
            temporaryLock.unlock();
        }
    }
}
