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
            // 用戶取消匹配
            if (needRemove.containsKey(next.getUserId())) {
                needRemove.remove(next.getUserId());
                maxHeap.remove(next);
                players.remove(next);
                continue;
            }

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

        // 新加入的用户加入匹配池
        try {
            temporaryLock.lock();
            players.addAll(temporary);
            maxHeap.addAll(temporary);
            temporary.clear();
        } finally {
            temporaryLock.unlock();
        }
    }
}
