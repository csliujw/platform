package com.platform.match.service;

public interface IMatchingService {
    String addPlayer(Integer userId, Integer rating, Integer botId);

    String removePlayer(Integer userId);
}
