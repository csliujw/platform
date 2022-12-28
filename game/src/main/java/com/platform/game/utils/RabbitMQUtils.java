package com.platform.game.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@Component
public class RabbitMQUtils {
    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final String RECORD_QUEUE = "record.queue";
    private static final String RATING_QUEUE = "rating.queue";
    private static final int TRY_MAX = 3;

    public void sendMsg2RecordQueue(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        // 失败暂时只记录日志，后面再做调整。
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        log.debug("消息发送成功, ID:{}", correlationData.getId());
                    } else {
                        log.error("消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        rabbitTemplate.convertAndSend(null, RECORD_QUEUE, message, correlationData);
    }

    public void sendMsg2RatingQueue(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        // 失败暂时只记录日志，后面再做调整。
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        log.debug("消息发送成功, ID:{}", correlationData.getId());
                    } else {
                        log.error("消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        rabbitTemplate.convertAndSend(null, RATING_QUEUE, message, correlationData);
    }
}
