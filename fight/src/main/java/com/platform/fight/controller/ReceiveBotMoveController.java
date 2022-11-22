package com.platform.fight.controller;

import com.platform.fight.service.interfaces.IReceiveBotMoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

// 接受 Bot 代碼的移動
@RestController
@RequestMapping("/pk/receive")
public class ReceiveBotMoveController {
    @Autowired
    private IReceiveBotMoveService receiveBotMoveService;

    @PostMapping("/bot/move/")
    public String receiveBotMove(@RequestParam MultiValueMap<String, String> data) {
        int userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("userId")));
        int direction = Integer.parseInt(Objects.requireNonNull(data.getFirst("direction")));
//        System.out.println("接收到了Bot的移动信息嘛" + userId + " " + direction); // 人机对战可以接收到机器的信息
        return receiveBotMoveService.receiveBotMove(userId, direction);
    }
}
