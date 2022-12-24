package com.platform.match.service.utils;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

//@Component

/**
 * @author payphone
 * @date 2022-12-13
 * 完成锁粒度的细化，使用 temporary 暂存需要匹配的人，用 temporaryLock 控制添加用户的并发。新增用户不再直接新增到匹配池中。
 */
public class LoopMatchingPool extends MatchingPool {

    private static List<Player> players = new ArrayList<>();

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        LoopMatchingPool.restTemplate = restTemplate;
    }

    protected void playerWithMatching(boolean[] used, int i, Player people) {
        if (people.getBotId() == -100) {
            // 如果是人机对战，在分配一个机器人给他。
            // 人机对战
            used[i] = true;
            Player machine = GenerateMachinePlayer.generateMachinePlayer();
            sendResult(people, machine);
        }
    }

    @Override
    public void increaseWaitingTime() {
        for (Player player : players) {
            if (player.getWaitingTime() <= 19) {
                player.setWaitingTime(player.getWaitingTime() + 1);
            }
        }
    }

    public LoopMatchingPool(){}
    public LoopMatchingPool(MatcherRule rule){
        this.matcherRule = rule;
    }

    @Override
    public void matchPlayers() {  // 尝试匹配所有玩家
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++) {
            Player people = players.get(i);
            // 已经被用过了，或者需要移除。此处可能出现，还没把元素 put 进去，就走到了 get 方法，这种情况认为用户取消不及时。是合理的。
            // hashmap 通过 hashCode+equals 判断是否是同一个对象的，而 Integer 类重写了 hashCode 方法，返回的就是数值
            if (used[i] || needRemove.containsKey(people.getUserId())) continue;

            // 判断是否是人机对战
            playerWithMatching(used, i, people);

            // 轮询匹配
            for (int j = i + 1; j < players.size(); j++) {
                if (used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if (matcherRule.matcher(a, b)) {
                    used[i] = used[j] = true;
                    sendResult(a, b);
                    break;
                }
            }
        }

        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            // 没有被使用且无需被移除的玩家需要重新加入集合，进行任务匹配。
            if (!used[i] && needRemove.get(i) == null) {
                newPlayers.add(players.get(i));
            } else {
                needRemove.remove(i);
            }
        }
        try {
            temporaryLock.lock();
            newPlayers.addAll(temporary);
            temporary.clear();
        } finally {
            temporaryLock.unlock();
        }
        players = newPlayers;
    }
}
