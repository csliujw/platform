package com.platform.fight;

import com.platform.fight.mapper.UserMapper;
import com.platform.fight.pojo.User;
import com.platform.fight.utils.CacheClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = "com.platform.fight")
class FightApplicationTests {

    @Autowired
    CacheClient cacheClient;

    @Autowired
    UserMapper mapper;


    @Test
    void testSet() {
        cacheClient.set("a", new User("payphone", 11, 0, null), 10L, TimeUnit.SECONDS);
    }

    @Test
    void testSetWithLogicExpire() {
        User payphone = new User("payphone", 11, 0, null);
        cacheClient.setWithLogicExpire("user:1", payphone, 10L, TimeUnit.SECONDS);
    }

    @Test
    void testQueryWithPassThrough() {
        cacheClient.queryWithPassThrough("user", 10086, User.class, mapper::selectById, 60L, TimeUnit.SECONDS);
    }

    @Test
    void testQueryWithLogicExpire() throws IOException {
        User user = cacheClient.queryWithLogicExpire("user:", 1, User.class,
                mapper::selectById, 60L, TimeUnit.SECONDS);
        System.in.read();
    }
}
