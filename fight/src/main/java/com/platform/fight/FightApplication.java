package com.platform.fight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("all")
public class FightApplication {

    // 用户登录用 redis 缓存一下
    // Bot 用 redis 缓存一下
    // rank list 缓存前 1000 名。启动时，缓存 rank list 前 1000 名的数据。无过期时间，常驻内存。天梯分进行更新时，
    // 查询 redis 中的最小值和当前用户的天梯分，如果当前用户的天梯分比最小值还低就不用找了。
    public static void main(String[] args) {
        SpringApplication.run(FightApplication.class, args);
    }
}
