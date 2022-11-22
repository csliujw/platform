package com.platform.fight.service.interfaces;

import com.alibaba.fastjson.JSONObject;

public interface IRankListService {
    JSONObject selectAll(Integer page);
}
