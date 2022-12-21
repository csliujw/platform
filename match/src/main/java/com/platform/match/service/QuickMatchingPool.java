package com.platform.match.service;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class QuickMatchingPool extends MatchingPool {
    private static TreeSet<Player> players = new TreeSet<>(Comparator.comparingInt(Player::getRating));
    private static ConcurrentHashMap<Integer, Boolean> needRemove = new ConcurrentHashMap<>();
    private static final ReentrantLock temporaryLock = new ReentrantLock();

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        QuickMatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        // TODO 减小锁粒度，用 temporary 作为容器暂存在轮询期间新加入的用戶。
        lock.lock();
        try {
            players.add(new Player(userId, rating, 10, botId));
            // 用戶有可能反复点击取消，加入，取消，加入，因此，加入新用户时需要 needRemove.remove,但是新加入用户和原先的用户是两个独立的集合
            // 也是先移除需要移除的用户，再加入新用户，因此此处不用特地写一句 needRemove.remove
            // needRemove.remove(userId);
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        // TODO 减小锁粒度，避免 removePlayer 与轮询 同时征用锁导致的并发粒度低。
        // TODO 采用 concurrentHashMap 标记那个用户需要被移除。
        lock.lock();
        try {
            Iterator<Player> iterator = players.iterator();
            while (iterator.hasNext()) {
                Player next = iterator.next();
                if (next.getUserId().equals(userId)) {
                    players.remove(next);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
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

    // 判断两名玩家是否匹配
    protected boolean checkMatched(Player a, Player b) {
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10;
    }


    @Override
    public void matchPlayers() {  // 尝试匹配所有玩家
        // TODO 改写方法，匹配结束后，获取 temporaryLock，將需要移除的用戶移除，并将新加入的用户加入待匹配用戶集合中。
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()) {
            Player next = iterator.next();
            // 人机对战，则继续轮询下一个用户
            if (playerWithMatching(next)) continue;
            Player higher = players.higher(next);
            Player lower = players.lower(next);

            // 无合适的匹配对象
            if (higher == null && lower == null) continue;

            if (higher != null && checkMatched(next, higher)) {
                sendResult(next, higher);
                System.out.println("匹配到高分段人选");
                iterator.remove();
                continue;
            }

            if (lower != null && checkMatched(next, lower)) {
                System.out.println("匹配到低分段人选");
                sendResult(next, lower);
                iterator.remove();
            }
        }
    }
}
