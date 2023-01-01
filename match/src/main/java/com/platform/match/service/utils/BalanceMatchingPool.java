package com.platform.match.service.utils;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;

// 待机时间长的用户优先匹配

/**
 * @author payphone
 * @date 2022-12-13
 * 完成锁粒度的细化，使用 temporary 暂存需要匹配的人，用 temporaryLock 控制添加用户的并发。新增用户不再直接新增到匹配池中。
 */
@Component
public class BalanceMatchingPool extends MatchingPool {

    private static PriorityQueue<Player> maxHeap = new PriorityQueue<>((a, b) -> b.getWaitingTime() - a.getWaitingTime());
    private static TreeSet<Player> players = new TreeSet<>(Comparator.comparingInt(Player::getRating));

    public BalanceMatchingPool() {
    }

    // 可更具需求自定义匹配规则
    public BalanceMatchingPool(MatcherRule rule) {
        this.matcherRule = rule;
    }

    protected boolean playerWithMatching(Player people) {
        if (people.getBotId() == -100) {
            // 如果是人机对战，在分配一个机器人给他。
            // 人机对战
            Player machine = GenerateMachinePlayer.generateMachinePlayer();
            sendResult(people, machine);
            return players.remove(people) && maxHeap.remove(people);
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


    private void removePlayer(Player player) {
        players.remove(player);
        maxHeap.remove(player);
    }

    @Override
    public void matchPlayers() {
        int size = maxHeap.size();
        // 遍历所有用户.
        for (int i = 0; i < size; i++) {
            Player next = maxHeap.peek();
            // 用戶取消匹配
            if (needRemove.containsKey(next.getUserId())) {
                needRemove.remove(next.getUserId());
                removePlayer(next);
                continue;
            }

            // 人机对战，则继续轮询下一个用户
            if (playerWithMatching(next)) continue;
            Player higher = players.higher(next);
            Player lower = players.lower(next);

            // 无合适的匹配对象,则完成了一个对象的遍历,i+1 即可
            if (higher == null && lower == null) continue;


            // 如果用户匹配到了对象,说明 i 还需要 ++(相当于一次遍历了两个元素)
            if (higher != null && matcherRule.matcher(next, higher)) {
                sendResult(next, higher);
                System.out.println("匹配到高分段人选");
                removePlayer(next);
                removePlayer(higher);
                i++;
                continue;
            }

            if (lower != null && matcherRule.matcher(next, lower)) {
                System.out.println("匹配到低分段人选");
                sendResult(next, lower);
                removePlayer(next);
                removePlayer(lower);
                i++;
            }
        }

        // 新加入的用户加入匹配池
        try {
            System.out.println("新用戶加入");
            temporaryLock.lock();
            players.addAll(temporary);
            maxHeap.addAll(temporary);
            temporary.clear();
        } finally {
            temporaryLock.unlock();
        }
    }
}
