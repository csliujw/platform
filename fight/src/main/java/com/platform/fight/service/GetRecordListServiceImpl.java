package com.platform.fight.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.fight.mapper.RecordMapper;
import com.platform.fight.mapper.UserMapper;
import com.platform.fight.pojo.Record;
import com.platform.fight.pojo.User;
import com.platform.fight.service.interfaces.IGetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Service
public class GetRecordListServiceImpl implements IGetRecordListService {

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private UserMapper userMapper;

    // 查询出页面中的信息
    public JSONObject getList(Integer page) {
        // 定义 Page，第 page 页，每页多条数据
        IPage<Record> recordIPage = new Page<>(page, 10);
        QueryWrapper<Record> query = new QueryWrapper<>();
        query.orderByDesc("createtime");

        // 对战双方的用户名和头像需要查询，然后做后处理。（查詢所有人的，显得热闹）
        List<Record> records = recordMapper.selectPage(recordIPage, query).getRecords();

        Set<Integer> ids = new TreeSet<>();
        for (Record cur : records) {
            // 要过滤掉机器人。
            // -100 是系統中存儲的一個機器人信息
            ids.add(cur.getAId() <= -1 ? -100 : cur.getAId());
            ids.add(cur.getBId() <= -1 ? -100 : cur.getBId());
        }

        // 查询出用户的信息，然后用 userId 和 photo username 做一个映射
        List<User> users = userMapper.selectBatchIds(ids);
        MultiValueMap<Integer, String> idToInfo = new LinkedMultiValueMap<>();
        for (int i = 0; i < users.size(); i++) {
            User curUser = users.get(i);
            idToInfo.put(curUser.getId(), Arrays.asList(curUser.getPhoto(), curUser.getUsername()));
        }
        JSONObject retVal = new JSONObject();

        List<JSONObject> items = new ArrayList<>();
        for (Record cur : records) {
            JSONObject item = new JSONObject();
            List<String> aInfo = idToInfo.get(cur.getAId() <= -1 ? -100 : cur.getAId());
            item.put("APhoto", aInfo.get(0));
            item.put("AUsername", aInfo.get(1));
            List<String> bInfo = idToInfo.get(cur.getBId() <= -1 ? -100 : cur.getBId());
            item.put("BPhoto", bInfo.get(0));
            item.put("BUsername", bInfo.get(1));
            // A 是藍 B 是紅
            if ("Red".equals(cur.getLoser())) {
                item.put("Result", "A/蓝方获胜");
            } else if ("Blue".equals(cur.getLoser())) {
                item.put("Result", "B/红方获胜");
            } else {
                item.put("Result", "平局");
            }
            item.put("record", cur);
            items.add(item);
        }

        retVal.put("records", items);
        retVal.put("recordsCount", recordMapper.selectCount(null));
        return retVal;
    }
}
