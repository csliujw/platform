package com.platform.match.service.utils;

import com.platform.match.dto.Player;
import com.platform.match.utils.GenerateMachinePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// 匹配池，对匹配池中的玩家进行对手的匹配。
@Component
// 可能玩家连接断开了，但是还在匹配池了，此时再发请求的话，就会有问题，因此需要修改。
@SuppressWarnings("all")
public class MatchingPool {

    private static List<Player> players = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;
    private static final String FIGHT_START_GAME_URL = "http://127.0.0.1:8080/pk/start/game/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        lock.lock();
        try {
            players.add(new Player(userId, rating, 8, botId));
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        lock.lock();
        try {
            List<Player> newPlayers = new ArrayList<>();
            for (Player player : players) {
                if (!player.getUserId().equals(userId)) {
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        } finally {
            lock.unlock();
        }
    }

    private void increaseWaitingTime() {  // 将所有当前玩家的等待时间加1
        for (Player player : players) {
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }

    private boolean checkMatched(Player a, Player b) {  // 判断两名玩家是否匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10;
    }

    // 返回结果的时候也要加上 botId
    private void sendResult(Player blue, Player red) {  // 返回匹配结果
        System.out.println("send result: " + blue + " " + red);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("blue_id", blue.getUserId().toString());
        data.add("blue_botId", blue.getBotId().toString());
        data.add("red_id", red.getUserId().toString());
        data.add("red_botId", red.getBotId().toString());
        // 如果 id 为 -100， botId 为 -100 说明匹配的是机器人。
        restTemplate.postForObject(FIGHT_START_GAME_URL, data, String.class);
    }

    private void matchPlayers() {  // 尝试匹配所有玩家
        System.out.println("match players: " + players.toString());
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++) {
            if (used[i]) continue;
            // 判断是否是人机对战
            Player people = players.get(i);
            if (people.getBotId() == -100) { // 如果是人机对战，在分配一个机器人给他。
                // 人机对战
                used[i] = true;
                Player machine = GenerateMachinePlayer.generateMachinePlayer();
                sendResult(people, machine);
            }

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
            if (!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }

    public void execute() {
        while (true) {
            try {
                Thread.sleep(1000);
                lock.lock();
                try {
                    increaseWaitingTime();
                    matchPlayers();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
