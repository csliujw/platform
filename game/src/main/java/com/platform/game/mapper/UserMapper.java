package com.platform.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.fight.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
