package com.platform.game.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.platform.fight.pojo.RedisData;
import com.platform.fight.pojo.User;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Payphone
 * @date 2022-12-15 23:16
 * TODO Redis 缓存工具类，封装缓存穿透，缓存击穿解决方案。通过测试用例，明天再将 redis 代码更换为工具类。
 */
@Component
@SuppressWarnings("all")
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    // 缓存 NULL 值的最大有效时间，单位秒
    private static final long CACHE_NULL_TTL = 60;
    private RedissonClient redissonClient;
    private static ThreadPoolExecutor CACHE_REBUILD_EXECUTOR = new ThreadPoolExecutor(20, 60, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(200));

    @Autowired
    public CacheClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String data = stringRedisTemplate.opsForValue().get(key);
        // 存在，则直接返回
        if (StrUtil.isNotBlank(data)) {
            return JSONUtil.toBean(data, type);
        }
        // 命中空值，发生缓存穿透，直接返回错误信息
        if (data != null) return null;

        // 如果只是未緩存到 Redis 中。
        R apply = dbFallBack.apply(id);

        if (apply == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        this.set(key, apply, time, unit);
        return apply;
    }

    public <R, ID> List<R> queryWithPassThroughList(String keyPrefix, ID id, Class<R> elementType,
                                                    Function<ID, List<R>> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String data = stringRedisTemplate.opsForValue().get(key);

        // 存在，则直接返回
        if (StrUtil.isNotBlank(data)) {
            JSONUtil.toList(data, elementType);
        }

        // 命中空值，发生缓存穿透，直接返回错误信息
        if (data != null) return null;

        // 如果只是未緩存到 Redis 中。
        List<R> apply = dbFallBack.apply(id);

        if (apply == null) {
            // 缓存空值
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }

        this.set(key, apply, time, unit);
        return apply;
    }

    public <R, ID> R queryWithLogicExpire(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallBack, Long time, TimeUnit unit) {

        String key = keyPrefix + id;
        String data = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(data)) return null;
        RedisData redisData = JSONUtil.toBean(data, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期
            return JSONUtil.toBean(redisData.getData().toString(), type);
        }

        // 过期，则重建缓存。为避免大量请求同时申请重建缓存，此处加锁，拿到锁的才能重建。沒拿到锁的返回旧数据
        RLock lock = redissonClient.getLock(keyPrefix + id + "_lock");
        try {
            boolean getLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
            if (getLock) {
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        System.out.println("開始衝擊");
                        R apply = dbFallBack.apply(id);
                        this.setWithLogicExpire(key, apply, time, unit);
                        System.out.println("成功设置逻辑过期");
                    } finally {
                        System.out.println("释放锁");
                        lock.unlock();
                    }
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 为避免长时间等待，直接返回旧数据
        return JSONUtil.toBean(redisData.getData().toString(), type);
    }

    public <R, Q> R queryHashWithPassThrough(String key, Q query, Class<R> elementType,
                                             Function<Q, R> dbFallBack, Long time, TimeUnit unit) {

        Map<Object, Object> data = stringRedisTemplate.opsForHash().entries(key);

        // 用戶在 redis 中
        if (data != null && data.size() != 0) {
            stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);
            return BeanUtil.mapToBean(data, elementType, true);
        }
        R r = dbFallBack.apply(query);

        if (r == null) {
            throw new RuntimeException("用户不存在");
        }

        setHash(key, r, elementType, time, unit);
        return r;
    }

    public <E> void setHash(String key, Object value, Class<E> elementType, Long time, TimeUnit unit) {
        Map<String, Object> map = BeanUtil.beanToMap(value, new HashMap<>(), CopyOptions
                .create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((k, v) -> v.toString()));

        // 用户移除敏感字段
        if (elementType == User.class) {
            System.out.println("是 user");
            map.remove("password");
        }
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }

}
