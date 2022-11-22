package com.platform.match.controller;

import com.platform.match.service.MatchingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class MatchingController {
    @Autowired
    private MatchingServiceImpl matchingService;

    @PostMapping("/player/add/")
    public String addPlayer(@RequestParam MultiValueMap<String, String> data) { // 一定是 MultiValueMap 用 map 不行，查下为什么
        // 匹配系统接收到信息后，将信息封装成玩家，添加进匹配池。在匹配池中进行游戏匹配
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("userId")));
        Integer rating = Integer.parseInt(Objects.requireNonNull(data.getFirst("rating")));
        Integer botId = Integer.parseInt(Objects.requireNonNull(data.getFirst("botId")));
        return matchingService.addPlayer(userId, rating,botId);
    }

    @PostMapping("/player/remove/")
    public String removePlayer(@RequestParam MultiValueMap<String, String> data) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("userId")));
        return matchingService.removePlayer(userId);
    }

}
