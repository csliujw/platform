package com.platform.fight.pojo;

import lombok.Data;

import java.time.LocalDateTime;

// 带逻辑时间的数据
@Data
public class RedisData {
    private Object data;
    private LocalDateTime expireTime;
}
