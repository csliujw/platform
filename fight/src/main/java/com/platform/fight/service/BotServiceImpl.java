package com.platform.fight.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.fight.mapper.BotMapper;
import com.platform.fight.pojo.Bot;
import com.platform.fight.pojo.User;
import com.platform.fight.service.interfaces.IBotService;
import com.platform.fight.service.utils.UserDetailsImpl;
import com.platform.fight.utils.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Bot 的 CRUD
 */
@Service
@SuppressWarnings("all")
public class BotServiceImpl implements IBotService {

    @Autowired
    private BotMapper botMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Map<String, String> add(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = principal.getUser();

        String title = data.get("title");

        String description = data.get("description");
        String content = data.get("content");
        Map<String, String> map = new HashMap<>();
        if (title == null || title.length() == 0) {
            map.put("resp_message", "标题不能为空");
            return map;
        }
        if (title.length() > 100) {
            map.put("error_message", "标题长度不能大于100");
            return map;
        }

        if (description == null || description.length() == 0) {
            description = "这个用户很懒，什么也没留下~";
        }

        if (description.length() > 300) {
            map.put("error_message", "Bot描述的长度不能大于300");
            return map;
        }

        if (content == null || content.length() == 0) {
            map.put("error_message", "代码不能为空");
            return map;
        }

        if (content.length() > 10000) {
            map.put("error_message", "代码长度不能超过10000");
        }

        QueryWrapper<Bot> query = new QueryWrapper<>();
        query.eq("user_id", user.getId());
        Long totalBot = botMapper.selectCount(query);
        if (totalBot != null && totalBot >= 10) {
            map.put("resp_message", "最多创建10个Bot！你无法继续创建！");
            return map;
        }

        Date now = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, now, now);
        botMapper.insert(bot);
        redisTemplate.delete(RedisKeyUtils.BOT_KEY + user.getId());
        map.put("resp_message", "success");
        return map;
    }

    /**
     * 是否有权限删除该 Bot
     *
     * @param data
     * @return
     */
    @Override
    public Map<String, String> remove(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = principal.getUser();
        Bot existBot = botMapper.selectById(data.get("bot_id"));

        Map<String, String> map = new HashMap<>();
        if (existBot == null) {
            map.put("resp_message", "Bot不存在或已被删除");
            return map;
        }
        if (existBot.getUserId().intValue() != user.getId().intValue()) {
            map.put("resp_message", "不是你的Bot，无权删除");
            return map;
        }
        botMapper.deleteById(existBot.getId());
        redisTemplate.delete(RedisKeyUtils.BOT_KEY + user.getId());
        map.put("resp_message", "success");
        return map;
    }

    @Override
    public Map<String, String> update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = principal.getUser();

        int botId = Integer.parseInt(data.get("bot_id"));
        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");

        Map<String, String> map = new HashMap<>();
        if (title == null || title.length() == 0) {
            map.put("error_message", "标题不能为空");
            return map;
        }
        if (title.length() > 100) {
            map.put("error_message", "标题长度不能大于100");
            return map;
        }

        if (description == null || description.length() == 0) {
            description = "这个用户很懒，什么也没留下~";
        }

        if (description.length() > 300) {
            map.put("error_message", "Bot描述的长度不能大于300");
            return map;
        }

        if (content == null || content.length() == 0) {
            map.put("error_message", "代码不能为空");
            return map;
        }

        if (content.length() > 10000) {
            map.put("error_message", "代码长度不能超过10000");
            return map;
        }

        QueryWrapper<Bot> query = new QueryWrapper<>();
        query.eq("user_id", user.getId()).eq("id", botId);

        Bot bot = new Bot(
                botId,
                user.getId(),
                title,
                description,
                content,
                null,
                new Date()
        );
        botMapper.update(bot, query);
        // 更新完后刪除緩存，爲了避免用戶响应时间过长，需要用到時再加載。
        redisTemplate.delete(RedisKeyUtils.BOT_KEY + user.getId());
        map.put("resp_message", "success");
        return map;
    }

    @Override
    public List<Bot> getList() {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = principal.getUser();

        // bot 用hash存比较合适，并不是所有时候都需要查询所有的信息，大多数时候不需要看内容。
        String key = RedisKeyUtils.BOT_KEY + user.getId();
        String value = redisTemplate.opsForValue().get(key);
        List<Bot> bots = null;
        if (value == null || "".equals(value.trim())) {
            // 说明 redis 中不存在
            QueryWrapper<Bot> query = new QueryWrapper<>();
            query.eq("user_id", user.getId());
            bots = botMapper.selectList(query);
            redisTemplate.opsForValue().set(key, JSONObject.toJSONString(bots), 30 * 60, TimeUnit.SECONDS);
        } else {
            JSONArray objects = JSONObject.parseArray(value);
            bots = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                bots.add(JSON.parseObject(objects.get(i).toString(), Bot.class));
            }
            redisTemplate.expire(key, 30 * 60, TimeUnit.SECONDS);
        }
        return bots;
    }
}
