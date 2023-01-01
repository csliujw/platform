package com.platform.game;


import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringAmqpTest {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSimpleQueue() throws IOException {
        // 队列名称
        String queueName = "hello.queue";
        // 消息
        String message = "hello, spring amqp!";
        // 发送消息，它怎么注册队列的？
        rabbitTemplate.convertAndSend(null, queueName, message);
        System.in.read();
    }
}