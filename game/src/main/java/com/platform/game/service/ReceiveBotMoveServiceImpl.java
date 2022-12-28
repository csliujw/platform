package com.platform.game.service;

import com.platform.game.consumer.WebSocketServer;
import com.platform.game.consumer.utils.Game;
import com.platform.game.service.interfaces.IReceiveBotMoveService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements IReceiveBotMoveService {
    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move:" + userId + " " + direction + " ");
        if (WebSocketServer.users.get(userId) != null) { // 需要把机器加入 users 中，游戲結束后把機器從 users 中一處
            Game game = WebSocketServer.users.get(userId).game;
            if (game != null) {
                if (game.getPlayerBlue().getId().equals(userId)) {
                    game.setNextStepBlue(direction);
                } else if (game.getPlayerRed().getId().equals(userId)) {
                    game.setNextStepRed(direction);
                }
            }
        }
        return "receive bot move success";
    }
}
