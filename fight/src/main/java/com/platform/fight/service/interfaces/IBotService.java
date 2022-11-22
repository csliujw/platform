package com.platform.fight.service.interfaces;


import com.platform.fight.pojo.Bot;

import java.util.List;
import java.util.Map;

public interface IBotService {
    Map<String, String> add(Map<String, String> map);

    Map<String, String> remove(Map<String, String> map);

    Map<String, String> update(Map<String, String> map);

    // 自己的 id 在 token 所以不用传参数
    List<Bot> getList();

}
