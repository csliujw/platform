package com.platform.game.controller;

import com.platform.game.consumer.WebSocketServer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping("/pk")
public class StartGameController {

    @PostMapping("/start/game/")
    public String startGame(@RequestParam MultiValueMap<String, String> data) {
        Integer blueId = Integer.parseInt(Objects.requireNonNull(data.getFirst("blue_id")));
        Integer blueBotId = Integer.parseInt(Objects.requireNonNull(data.getFirst("blue_botId")));
        Integer redId = Integer.parseInt(Objects.requireNonNull(data.getFirst("red_id")));
        Integer redBotId = Integer.parseInt(Objects.requireNonNull(data.getFirst("red_botId")));
        WebSocketServer.startGame(blueId, blueBotId, redId, redBotId);
        return "start game success";
    }

}
