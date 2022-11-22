package com.platform.fight.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.fight.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RankListMapper extends BaseMapper<User> {
}
