package com.platform.bot.service;

public interface BotRunningService {
    // input 地图信息，对手走过的路径，障碍物等
    String addBot(Integer userId, String botCode, String input);
}
