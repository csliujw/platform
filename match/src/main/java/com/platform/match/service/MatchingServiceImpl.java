package com.platform.match.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 2022-12-21 修改匹配规则。匹配套用策略模式。根据不同情况注入不同的策略。
 */
@Service
public class MatchingServiceImpl implements IMatchingService {

    public static MatchingPool matchingPool;

    @Autowired
    @Qualifier("quickMatchingPool")
    public void setMatchingPool(MatchingPool matchingPool) {
        MatchingServiceImpl.matchingPool = matchingPool;
    }

    @Override
    public String addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("add player: " + userId + " " + rating + " " + botId);
        matchingPool.addPlayer(userId, rating, botId);
        return "add player success";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove player: " + userId);
        matchingPool.removePlayer(userId);
        return "remove player success";
    }
}
