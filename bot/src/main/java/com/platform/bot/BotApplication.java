package com.platform.bot;

import com.platform.bot.service.BotRunningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class BotApplication {

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        // 会吃掉异常
        threadPool.submit(BotRunningServiceImpl.botPool::execute);
        SpringApplication.run(BotApplication.class, args);
    }
}
