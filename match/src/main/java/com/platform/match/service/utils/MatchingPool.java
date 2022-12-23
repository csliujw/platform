package com.platform.match.service.utils;

import com.platform.match.dto.Player;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// 匹配池，对匹配池中的玩家进行对手的匹配。
// 可能玩家连接断开了，但是还在匹配池了，此时再发请求的话，就会有问题，因此需要修改。
@SuppressWarnings("all")
public abstract class MatchingPool {

    protected static RestTemplate restTemplate;
    protected static final String FIGHT_START_GAME_URL = "http://127.0.0.1:8080/pk/start/game/";
    protected static final ReentrantLock lock = new ReentrantLock();
    protected static List<Player> temporary = new ArrayList<>();
    protected static ConcurrentHashMap<Integer, Boolean> needRemove = new ConcurrentHashMap<>();
    protected static final ReentrantLock temporaryLock = new ReentrantLock();

    // 这里考虑到并发粒度的问题，额外采用一个 tmpList 暂存新加入的用户。
    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        temporaryLock.lock();
        try {
            // 用戶有可能反复点击取消，加入，取消，加入，因此，加入新用户时需要 needRemove.remove,但是新加入用户和原先的用户是两个独立的集合
            // 也是先移除需要移除的用户，再加入新用户，因此此处不用特地写一句 needRemove.remove
            temporary.add(new Player(userId, rating, 8, botId));
        } finally {
            temporaryLock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        needRemove.put(userId, true);
    }

    // 增加游戏用户的等待时间
    public abstract void increaseWaitingTime();

    // 匹配所有用户
    public abstract void matchPlayers();

    public void execute() {
        while (true) {
            try {
                Thread.sleep(1500);
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

    // 发送结果
    // 返回结果的时候也要加上 botId
    protected void sendResult(Player blue, Player red) {  // 返回匹配结果
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("blue_id", blue.getUserId().toString());
        data.add("blue_botId", blue.getBotId().toString());
        data.add("red_id", red.getUserId().toString());
        data.add("red_botId", red.getBotId().toString());
        // 如果 id 为 -100， botId 为 -100 说明匹配的是机器人。
        try {
            restTemplate.postForObject(FIGHT_START_GAME_URL, data, String.class);
        } catch (Exception e) {
            throw new RuntimeException("restTemplate 调用异常！");
        }
    }

}
