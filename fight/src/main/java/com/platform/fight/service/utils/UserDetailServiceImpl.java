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
        System.out.println("spring security");// 用户登录时会触发这个方法吗？
        User user = getUser(username);
        return new UserDetailsImpl(user);
    }

    public User getUser(String username) {

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisKeyUtils.USER_KEY + username);
        // 用戶在 redis 中
        User user = null;
        if (entries != null && entries.size() != 0) {
            user = BeanUtil.mapToBean(entries, User.class, true);
        } else {
            QueryWrapper query = new QueryWrapper<User>();
            query.eq("username", username);
            user = userMapper.selectOne(query);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            } else {
                // 不在则存入并设置过期时间
                Map<String, Object> map = BeanUtil.beanToMap(user, new HashMap<>(),
                        CopyOptions.create().
                                setIgnoreNullValue(true).
                                setFieldValueEditor((key, value) -> value.toString()));
                redisTemplate.opsForHash().putAll(RedisKeyUtils.USER_KEY + username.trim(), map);
                redisTemplate.expire(RedisKeyUtils.USER_KEY + username.trim(), 30 * 60, TimeUnit.SECONDS);
            }
        }
        return user;
    }
}
