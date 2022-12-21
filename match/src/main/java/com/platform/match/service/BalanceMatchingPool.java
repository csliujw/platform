package com.platform.match.service;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;

import java.util.*;

// 待机时间长的用户有限匹配
// TODO 与 QuickMatchingPool 类似
public class BalanceMatchingPool extends MatchingPool {

    PriorityQueue<Player> maxHeap = new PriorityQueue<>((a, b) -> b.getWaitingTime() - a.getWaitingTime());
    private static Random randomSelectOpponent = new Random();
    private static TreeSet<Player> players = new TreeSet<>(Comparator.comparingInt(Player::getRating));


    @Override
    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        lock.lock();
        try {
            Player player = new Player(userId, rating, 10, botId);
            players.add(player);
            maxHeap.add(player);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removePlayer(Integer userId) {
        lock.lock();
        try {
            Iterator<Player> iterator = players.iterator();
            while (iterator.hasNext()) {
                Player next = iterator.next();
                if (next.getUserId().equals(userId)) {
                    players.remove(next);
                    maxHeap.remove(next);
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

    // 判断两名玩家是否匹配
    protected boolean checkMatched(Player a, Player b) {
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10;
    }


    @Override
    public void matchPlayers() {
        for (int i = 0; i < maxHeap.size(); i++) {
            Player next = maxHeap.peek();
            // 人机对战，则继续轮询下一个用户
            if (playerWithMatching(next)) continue;
            Player higher = players.higher(next);
            Player lower = players.lower(next);

            // 无合适的匹配对象
            if (higher == null && lower == null) continue;

            if (higher != null && checkMatched(next, higher)) {
                sendResult(next, higher);
                System.out.println("匹配到高分段人选");
                players.remove(next);
                maxHeap.remove(next);
                continue;
            }

            if (lower != null && checkMatched(next, lower)) {
                System.out.println("匹配到低分段人选");
                sendResult(next, lower);
                players.remove(next);
                maxHeap.remove(next);
            }
        }
    }
}
