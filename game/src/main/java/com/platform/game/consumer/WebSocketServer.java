package com.platform.game.consumer;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.platform.fight.pojo.Bot;
import com.platform.fight.pojo.User;
import com.platform.fight.utils.RedisKeyUtils;
import com.platform.game.consumer.utils.Game;
import com.platform.game.consumer.utils.JWTAuthentication;
import com.platform.game.mapper.BotMapper;
import com.platform.game.utils.MachinePlayerUtils;
import com.platform.game.utils.RabbitMQUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
// 注意不要以'/'结尾
@ServerEndpoint("/websocket/{token}")
@SuppressWarnings("all")
// 不是 Spring 的组件，不是单例的，是多例的
public class WebSocketServer {

    public static StringRedisTemplate stringRedisTemplate;
    public static RabbitMQUtils rabbitMQUtils;
    public static RestTemplate restTemplate;
    public static BotMapper botMapper;

    public Game game = null;

    // 记录用户 id 和会话的关系
    public static final ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    // 那个用户的会话
    private User user;
    // 会话
    private Session session = null;
    private static final String MATCH_ADD_URL = "http://localhost:8081/player/add/";
    private static final String MATCH_REMOVE_URL = "http://localhost:8081/player/remove/";
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 50, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(500));

    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setRabbitMQUtils(RabbitMQUtils rabbitMQUtils) {
        WebSocketServer.rabbitMQUtils = rabbitMQUtils;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        WebSocketServer.stringRedisTemplate = stringRedisTemplate;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;

        String userName = JWTAuthentication.getUserName(token);
        String userStr = stringRedisTemplate.opsForValue().get(RedisKeyUtils.USER_KEY + userName);

        this.user = (userStr != null && !"".equals(userStr)) ? JSONUtil.toBean(userStr, User.class) : null;

        if (this.user != null) {
            users.put(this.user.getId(), this);
            // checkUser 需要用到用戶信息。做一个 key-value 的映射
            System.out.println("key-value 映射");
            stringRedisTemplate.opsForValue().set(RedisKeyUtils.USER_KEY + user.getId(), RedisKeyUtils.USER_KEY + userName, 30, TimeUnit.MINUTES);
        } else {
            this.session.close();
        }
    }

    @OnClose
    public void onClose() throws IOException {
        // 关闭会话，移除用户，移除匹配池，可能匹配成功的一瞬间，用户断开连接了（故意关闭窗口）此时要进行特判。但是还会把他匹配到一块
        // 因为几秒后就会判断他输了
        if (this.user != null) {
            users.remove(user.getId());
            stringRedisTemplate.delete(RedisKeyUtils.USER_KEY + user.getId());
        }
    }

    public static void startGame(Integer blueId, Integer blueBotId, Integer redId, Integer redBotId) {
        // 红蓝双方 id,如果是机器人，则会自动生成一个机器人。
        User blueUser = checkAndSetUser(blueId);
        User redUser = checkAndSetUser(redId);


        Bot blueBot = checkAndSetBot(blueId, blueBotId);
        Bot redBot = checkAndSetBot(redId, redBotId);

        Game game = new Game(13,
                14,
                20,
                blueUser.getId(),
                blueBot,
                redUser.getId(),
                redBot);

        game.mapUtil.createMap();

        // 说明是机器人，需要将机器人加入 users 中
        if (blueUser.getId() != null && blueUser.getId() < -1) {
            users.put(blueUser.getId(), new WebSocketServer());
        }
        if (redUser.getId() != null && redUser.getId() < -1) {
            users.put(redUser.getId(), new WebSocketServer());
        }

        if (users.get(blueUser.getId()) != null) {
            users.get(blueUser.getId()).game = game;
        }
        if (users.get(redUser.getId()) != null) {
            users.get(redUser.getId()).game = game;
        }

        // 提交到线程池中执行
        threadPool.submit(game::run);

        JSONObject respGame = new JSONObject();
        respGame.put("blue_id", game.getPlayerBlue().getId());
        respGame.put("blue_sx", game.getPlayerBlue().getSx());
        respGame.put("blue_sy", game.getPlayerBlue().getSy());
        respGame.put("red_id", game.getPlayerRed().getId());
        respGame.put("red_sx", game.getPlayerRed().getSx());
        respGame.put("red_sy", game.getPlayerRed().getSy());
        respGame.put("gamemap", game.mapUtil.getGameMap());

        // 生成对局信息，并发送给双方。
        generateOpponentAndGameInfo(blueUser, redUser, respGame);
        generateOpponentAndGameInfo(redUser, blueUser, respGame);
    }

    public void startMatching(Integer botId) {
        // System.out.println("start matching, 当前的匹配类型是" + botId);
        // 向匹配系统发送请求
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("botId", botId.toString()); // 由 Bot 系统判断，派什么出战。
        restTemplate.postForObject(MATCH_ADD_URL, data, String.class);
    }

    public void stopMatching() {
        // System.out.println("stop matching");
        // 向匹配系统发送请求
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", this.user.getId().toString());
        restTemplate.postForObject(MATCH_REMOVE_URL, data, String.class);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // System.out.format("receive message %s", message);
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");

        if ("start-matching".equals(event)) {
            Integer bootId = data.getInteger("botId");
            startMatching(bootId);
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            System.out.println(this.user.getUsername() + " " + data.getInteger("direction"));
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateOpponentAndGameInfo(User me, User opponent, JSONObject respGame) {
        JSONObject blueResp = new JSONObject();
        blueResp.put("event", "start-matching");
        blueResp.put("opponent_username", opponent.getUsername());
        blueResp.put("opponent_photo", opponent.getPhoto());
        blueResp.put("game", respGame);
        // 用户在线，且用户不是机器人（id>=-1） 才需要发送消息
        if (users.get(me.getId()) != null && me.getId() >= -1) {
            users.get(me.getId()).sendMessage(blueResp.toJSONString());
        }
    }

    private void move(int direction) {
        if (game.getPlayerBlue().getId().equals(user.getId())) {
            // 人工操作,才需要接受前端的操作（人的輸入）
            if (game.getPlayerBlue().getBotId().equals(-1)) {
                game.setNextStepBlue(direction);
            }
        }
        if (game.getPlayerRed().getId().equals(user.getId())) {
            if (game.getPlayerRed().getBotId().equals(-1)) {
                game.setNextStepRed(direction);
            }
        }
    }

    private static Bot checkAndSetBot(Integer userId, Integer botId) {
        if (botId >= 0) {
//            String botStr = stringRedisTemplate.opsForValue().get(RedisKeyUtils.BOT_KEY + botId);
//            System.out.println("Bot Code:" + botStr);
//            return (botStr != null && !"".equals(botStr)) ? JSONUtil.toBean(botStr, Bot.class) : null;
            return botMapper.selectById(botId);
        } else if (userId < -1 && botId < -1) {// 如果 blueId < 0 且 botId < -1 说明是机器人
            // 判断是否是机器，如果是机器会返回生成的一个机器人。
            return MachinePlayerUtils.judgeIsMachinePlayer(botId);
        }
        return null;
    }

    // 检查是否是机器人。
    private static User checkAndSetUser(Integer userId) {
        // 真实用户的id都是大于等于0的
        String machinePhoto = "https://cdn.acwing.com/media/user/profile/photo/2675_lg_92591a4803.jpeg";
        if (userId <= -1) {
            // 説明是機器人
            return new User(userId, "机器人", "", machinePhoto, 1000, 0);
        }
        String key = stringRedisTemplate.opsForValue().get(RedisKeyUtils.USER_KEY + userId);
        String userStr = stringRedisTemplate.opsForValue().get(key);
        return (userStr != null && !"".equals(userStr)) ? JSONUtil.toBean(userStr, User.class) : null;
        // return userMapper.selectById(userId);
    }
}
