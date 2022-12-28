package com.platform.fight.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.fight.mapper.RankListMapper;
import com.platform.fight.pojo.User;
import com.platform.fight.service.interfaces.IRankListService;
import com.platform.fight.utils.RedisKeyUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("all")
// 緩存前 1000 名
public class RankListServiceImpl implements IRankListService {
    private Semaphore rebuildCacheLock = new Semaphore(1);

    @Autowired
    private RankListMapper ranklistMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public void saveToRedis() {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.gt("id", -1).orderByDesc("rating");
        query.last(" limit 0,1000");
        List<User> users = ranklistMapper.selectList(query);
        String rankListKey = RedisKeyUtils.RANK_LIST_KEY;
        String countKey = RedisKeyUtils.RANK_COUNT_KEY;

        // 批量添加，50 个一组
        Set<ZSetOperations.TypedTuple<String>> set = new HashSet<>();
        for (int i = 0; i < users.size(); i++) {
            User cur = users.get(i);
            set.add(new DefaultTypedTuple<String>(cur.getUsername() + ":" + cur.getRating() + ":" + cur.getCount() + ":" + cur.getPhoto(), cur.getRating().doubleValue()));
            if ((i + 1) % 50 == 0) {
                redisTemplate.opsForZSet().add(rankListKey, set);
                set.clear();
            }
        }

        // 如果没有数据，则缓存一个空数据，数据过期时间设置为 30s 避免缓存穿透。
        if (set.size() == 0) {
            redisTemplate.opsForZSet().add(rankListKey, "NULL:0:0:0", 0);
            redisTemplate.expire(rankListKey, 30, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForZSet().add(rankListKey, set);
            redisTemplate.opsForValue().set(countKey, String.valueOf((int) (Math.ceil(users.size() / 10.0))));
            redisTemplate.expire(rankListKey, 120, TimeUnit.SECONDS); // 2 分钟刷新一次排行榜。
            set.clear();
        }
    }


    /**
     * 使用分布式锁重建缓存
     *
     * @param page
     * @return
     */
    public boolean rebuildRedisCacheByRedission(Integer page) {
        // 需要重建緩存，重建缓存的话要避免多个用户一起重建缓存。单体应用的话最简单的就是信号量。
        List<User> users = null;
        String rankListKey = RedisKeyUtils.RANK_LIST_KEY;
        boolean retVal = false;
        RLock lock = redissonClient.getLock("ranklist:lock");
        try {
            boolean getLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (getLock) {
                saveToRedis();
                retVal = true;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("重建缓存失败：" + e.getMessage());
        } finally {
            lock.unlock();
        }
        return retVal;
    }

    /**
     * TODO: 单机重建缓存，用信号量。
     *
     * @param page
     * @return
     */
    public boolean rebuildRedisCache(Integer page) {
        // 需要重建緩存，重建缓存的话要避免多个用户一起重建缓存。单体应用的话最简单的就是信号量。
        List<User> users = null;
        String rankListKey = RedisKeyUtils.RANK_LIST_KEY;
        boolean retVal = false;
        try {
            rebuildCacheLock.tryAcquire(1, 1, TimeUnit.SECONDS);
            saveToRedis();
            retVal = true;
        } catch (Exception e) {
            throw new RuntimeException("重建缓存失败：" + e.getMessage());
        } finally {
            rebuildCacheLock.release();
            return retVal;
        }
    }

    @Override
    public JSONObject selectAll(Integer page) {
        String rankListKey = RedisKeyUtils.RANK_LIST_KEY;
        String countKey = RedisKeyUtils.RANK_COUNT_KEY;
        int i = Integer.parseInt(rankListKey.split(":")[1]);

        // 需要返回的数据
        List<User> records = null;
        JSONObject retVal = new JSONObject();
        if (page * 10 < i) {
            // 前 xx 名，从 redis 中查询
            records = queryRecordByRedis(page, rankListKey);
            if (records.size() == 0) { // 无数据则重建缓存。
                // 重建成功，然后查出数据
                if (rebuildRedisCache(page)) {
                    records = queryRecordByRedis(page, rankListKey);
                } else {
                    // 重建失败，重新尝试查询数据。
                    records = queryRecordByRedis(page, rankListKey);
                }
            }
        }

        int totalPage = Integer.parseInt(redisTemplate.opsForValue().get(countKey));
        retVal.put("total_pages", totalPage);
        retVal.put("records", records);
        return retVal;
    }

    private List<User> queryRecordByRedis(Integer page, String rankListKey) {
        List<User> records = new ArrayList<>();
        Set<String> range = redisTemplate.opsForZSet().reverseRange(rankListKey, (page - 1) * 10, page * 10);
        Iterator<String> iterator = range.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            String[] split = next.split(":");
            User user = new User(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), split[3]);
            records.add(user);
        }
        return records;
    }

}
