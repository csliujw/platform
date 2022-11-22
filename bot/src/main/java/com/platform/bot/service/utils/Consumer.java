package com.platform.bot.service.utils;

import com.platform.bot.dto.BotDTO;
import com.platform.bot.utils.BotCodeInterface;
import com.platform.bot.utils.DynamicCompiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.*;


// 控制代码的执行时间
@Component
public class Consumer extends Thread {
    private BotDTO bot;
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

    private static RestTemplate restTemplate;
    private static final String RECEIVE_BOT_MOVE_URL = "http://127.0.0.1:8080/pk/receive/bot/move/";

    private static final DynamicCompiler dynamicCompiler = new DynamicCompiler();
    private static BotCodeInterface botCodeInterface;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, TimeUnit unit, BotDTO bot) {
        this.bot = bot;
        Future<Boolean> submit = threadPoolExecutor.submit(() -> {
            start();
            return true;
        });
        try {
            // 设置最大执行时间
            submit.get(timeout, unit);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    // 在code中的Bot类名后添加uid
    private static String addUid(String code, String uid) {
        int k = code.indexOf(" implements com.platform.bot.utils.BotCodeInterface");
        return code.substring(0, k) + uid + code.substring(k);
    }

    private void runCode() {
        Integer direction = botCodeInterface.nextMove(bot.getInput());
        // 藍色是1
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", bot.getUserId().toString());
        data.add("direction", direction.toString());
        restTemplate.postForObject(RECEIVE_BOT_MOVE_URL, data, String.class);
    }

    @Override
    public void run() {
        if (botCodeInterface == null) {
            UUID uuid = UUID.randomUUID();
            String uid = uuid.toString().substring(0, 8);
            try {
                Class<?> aClass = dynamicCompiler.compileToClass("com.platform.bot.service.BotCode" + uid, addUid(bot.getBotCode(), uid));
                botCodeInterface = (BotCodeInterface) aClass.getDeclaredConstructor().newInstance();
                runCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            runCode();
        }
    }
}
