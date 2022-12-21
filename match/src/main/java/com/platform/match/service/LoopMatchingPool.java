package com.platform.match.service;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LoopMatchingPool extends MatchingPool {

    private static List<Player> players = new ArrayList<>();
    private static List<Player> temporary = new ArrayList<>(); // 细化加锁粒度，增加并发度
    private static ReentrantLock temporaryLock = new ReentrantLock(); // 细化加锁粒度，增加并发度
    private static ConcurrentHashMap<Integer, Boolean> needRemove = new ConcurrentHashMap<>();

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        LoopMatchingPool.restTemplate = restTemplate;
    }

    // 这里考虑到并发粒度的问题，额外采用一个 tmpList 暂存新加入的用户。
    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        temporaryLock.lock();
        try {
            temporary.add(new Player(userId, rating, 8, botId));
        } finally {
            temporaryLock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        needRemove.put(userId, true);
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

    // 判断两名玩家是否匹配
    protected boolean checkMatched(Player a, Player b) {
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10;
    }


    @Override
    public void matchPlayers() {  // 尝试匹配所有玩家
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++) {
            // 已经被用过了，或者需要移除。此处可能出现，还没把元素 put 进去，就走到了 get 方法，这种情况认为用户取消不及时。是合理的。
            if (used[i] || needRemove.get(i) != null) continue;
            // 判断是否是人机对战
            Player people = players.get(i);
            playerWithMatching(used, i, people);

            for (int j = i + 1; j < players.size(); j++) {
                if (used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if (checkMatched(a, b)) {
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
        } finally {
            temporaryLock.unlock();
        }
        players = newPlayers;
    }
}
