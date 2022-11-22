package com.platform.fight.service.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.fight.mapper.UserMapper;
import com.platform.fight.pojo.User;
import com.platform.fight.utils.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Spring security 通过它查询数据库，看用户信息是否存在。
@Service
@SuppressWarnings("all")
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUser(username);
        return new UserDetailsImpl(user);
    }

    // 一次完整的请求中，可能有多个服务需要使用到 User 信息，这里做一个调整，每次请求时从 Redis 查询用户信息，然后存入 ThreadLocal 中，
    // 后面的应用需要再使用时就无需再次请求 Redis 了。
    public User getUser(String username) {
        String keys = RedisKeyUtils.USER_KEY + username.trim();
        User user = null;

        Map<Object, Object> userInfomation = redisTemplate.opsForHash().entries(keys);

        // 用戶在 redis 中
        if (userInfomation != null && userInfomation.size() != 0) {
            user = BeanUtil.mapToBean(userInfomation, User.class, true);
            redisTemplate.expire(keys, 30 * 60, TimeUnit.SECONDS);
            return user;
        }

        user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 不在则存入 Redis 并设置过期时间
        Map<String, Object> map = BeanUtil.beanToMap(user, new HashMap<>(), CopyOptions
                .create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((key, value) -> value.toString()));

        redisTemplate.opsForHash().putAll(keys, map);
        redisTemplate.expire(keys, 30 * 60, TimeUnit.SECONDS);
        return user;
    }
}
