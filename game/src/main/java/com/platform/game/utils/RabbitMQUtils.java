package com.platform.game.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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


    private static final String RECORD_QUEUE = "records.queue";
    private static final String RATING_QUEUE = "ratings.queue";
    private static final String EXCHANGE_NAME = "simples.direct";

    public void sendMsg2RecordQueue(String message) {
        CorrelationData correlationData = getCorrelationData();
        // 失败暂时只记录日志，后面再做调整。
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        log.info("record 消息发送成功, ID:{}", correlationData.getId());
                    } else {
                        log.error("record 消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("record 消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "records", message, correlationData);
    }

    @NotNull
    private CorrelationData getCorrelationData() {
        return new CorrelationData(UUID.randomUUID().toString());
    }

    public void sendMsg2RatingQueue(String message) {
        CorrelationData correlationData = getCorrelationData();

        // 失败暂时只记录日志，后面再做调整。
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        log.info("rating 消息发送成功, ID:{}", correlationData.getId());
                    } else {
                        log.error("rating 消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("rating 消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "ratings", message, correlationData);
    }
}
