package com.platform.fight.mq;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.platform.fight.mapper.RecordMapper;
import com.platform.fight.pojo.Record;
import com.platform.fight.pojo.User;
import com.platform.fight.pojo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public
class RabbitListenerMessage {

    @Autowired
    private RecordMapper recordMapper;

    @RabbitListener(queues = "record.queue")
    public void listenRecordQueueMessage(String msg) {
        Record record = JSONUtil.toBean(msg, Record.class);
        recordMapper.insert(record);
        log.info("消费者接收到消息：【{}】", record);
    }

    @RabbitListener(queues = "rating.queue")
    public void listenRatingQueueMessageOne(String msg) {
        UserDTO userDTO = JSONUtil.toBean(msg, UserDTO.class);
        User user = userDTO.getUser();
        boolean result = userDTO.isResult();
        int rating = result ? 5 : -5;
        SqlRunner.db().update("update user set rating = rating + {0},count=count+1 where id = {1}", rating, user.getId());
        log.info("消费者接收到消息：【{}】", userDTO);
    }

    @RabbitListener(queues = "rating.queue")
    public void listenRatingQueueMessageTwo(String msg) {
        UserDTO userDTO = JSONUtil.toBean(msg, UserDTO.class);
        User user = userDTO.getUser();
        boolean result = userDTO.isResult();
        int rating = result ? 5 : -5;
        SqlRunner.db().update("update user set rating = rating + {0},count=count+1 where id = {1}", rating, user.getId());
        log.info("消费者接收到消息：【{}】", userDTO);
    }
}
