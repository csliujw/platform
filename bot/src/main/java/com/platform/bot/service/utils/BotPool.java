package com.platform.bot.service.utils;

import com.platform.bot.dto.BotDTO;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BotPool extends Thread {

    private final BlockingQueue<BotDTO> botsQueue = new LinkedBlockingQueue<>();

    // Controller 的线程会调用这个方法
    public void addBot(Integer userId, String botCode, String input) {
        botsQueue.add(new BotDTO(userId, botCode, input));
    }

    // 消费一个bot，设置最大运行时间为 2s
    private void consume(BotDTO bot) {
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);
    }

    // BotPool 也会执行这个方法。多线程争用 botsQueue
    @Override
    public void run() {
        while (true) {
            try {
                BotDTO bot = botsQueue.take();
                consume(bot);  // 比较耗时，可能会执行几秒钟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
