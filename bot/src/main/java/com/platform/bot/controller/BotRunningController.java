package com.platform.bot.controller;

import com.platform.bot.service.BotRunningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 添加从其他微服务传递过来的bot
 */
@RestController
public class BotRunningController {
    @Autowired
    private BotRunningService botRunningService;

    @PostMapping("/bot/add/")
    public String addBot(@RequestParam MultiValueMap<String, String> data) {
        // 用户 id
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("userId")));
        // bot 代码
        String botCode = data.getFirst("botCode");
        // 当前的对局信息，红方坐标位置，蓝方坐标位置，地图信息
        String input = data.getFirst("input");
        return botRunningService.addBot(userId, botCode, input);
    }
}
