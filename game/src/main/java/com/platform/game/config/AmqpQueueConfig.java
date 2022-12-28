package com.platform.game.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpQueueConfig {
    //     private static final String RECORD_QUEUE = "record.queue";
    //    private static final String RATING_QUEUE = "rating.queue";
    @Bean
    public Queue recordQueue() {
        return new Queue("record.queue");
    }

    @Bean
    public Queue ratingQueue() {
        return new Queue("rating.queue");
    }
}
