package com.platform.bot.service;

import com.platform.bot.service.utils.BotPool;
import org.springframework.stereotype.Service;

/**
 * 添加bot到bot运行池中
 */
@Service
public class BotRunningServiceImpl implements BotRunningService {
    public final static BotPool botPool = new BotPool();

    @Override
    public String addBot(Integer userId, String botCode, String input) {
        botPool.addBot(userId, botCode, input);
        return "add bot success";
    }
}
