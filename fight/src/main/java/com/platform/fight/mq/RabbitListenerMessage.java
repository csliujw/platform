package com.platform.fight.mq;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.platform.fight.mapper.RecordMapper;
import com.platform.fight.pojo.Record;
import com.platform.fight.pojo.User;
import com.platform.fight.pojo.UserDTO;
import com.platform.fight.utils.RedisKeyUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public
class RabbitListenerMessage {

    @Autowired
    private RecordMapper recordMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    // queues = "records.queue"
    @RabbitListener(bindings = @QueueBinding(value = @Queue("records.queue"),
            exchange = @Exchange(value = "simples.direct", type = ExchangeTypes.DIRECT),
            key = {"records"}
    ))
    public void listenRecordQueueMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getHeader("spring_returned_message_correlation").toString();
        // 重复消费会被丢弃
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(messageId, "1", 60, TimeUnit.SECONDS);
        try {
            if (aBoolean == null) {
                channel.basicReject(deliveryTag, true);// requeue 重回队列尾部
            } else if (Boolean.TRUE.equals(aBoolean)) {
                // true 説明，不存在設置成功，可以正常消費
                String msg = new String(message.getBody());
                Record record = JSONUtil.toBean(msg, Record.class);
                recordMapper.insert(record);
                log.info("消费者接收到消息：【{}】", record);
                channel.basicAck(deliveryTag, true);
            } else {
                log.info("重复消费，丢弃消息 {}", new String(message.getBody()));
                channel.basicNack(deliveryTag, true, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue("ratings.queue"),
            exchange = @Exchange(value = "simples.direct", type = ExchangeTypes.DIRECT),
            key = {"ratings"}
    ))
    public void listenRatingQueueMessageOne(Message message, Channel channel) {
        consumer(message, channel);
    }

    private void consumer(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getHeader("spring_returned_message_correlation").toString();
        log.info("=========================================");
        log.info("rating 当前消息的唯一id = {}", messageId);
        log.info("=========================================");

        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(messageId, "1", 60, TimeUnit.SECONDS);
        // 如果為 null 或存在（説明消費過了）
        try {
            if (aBoolean == null) {
                channel.basicNack(deliveryTag, true, true); // requeue 重回队列尾部
            } else if (Boolean.TRUE.equals(aBoolean)) {
                // true 説明，不存在設置成功，可以正常消費
                String msg = new String(message.getBody());
                UserDTO userDTO = JSONUtil.toBean(msg, UserDTO.class);
                User user = userDTO.getUser();
                boolean result = userDTO.isResult();
                int rating = result ? 5 : -5;
                SqlRunner.db().update("update user set rating = rating + {0},count=count+1 where id = {1}", rating, user.getId());
                String name = stringRedisTemplate.opsForValue().get(RedisKeyUtils.USER_KEY + user.getId());
                stringRedisTemplate.opsForHash().increment(RedisKeyUtils.USER_KEY + name, "rating", rating);
                stringRedisTemplate.opsForHash().increment(RedisKeyUtils.USER_KEY + name, "count", 1);
                log.info("消费者接收到消息：{}", userDTO);
                channel.basicAck(deliveryTag, true);
            } else {
                log.info("重复消费，丢弃消息 {}", new String(message.getBody()));
                channel.basicNack(deliveryTag, true, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
