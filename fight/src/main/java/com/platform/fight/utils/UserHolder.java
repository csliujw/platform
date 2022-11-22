package com.platform.fight.utils;

import com.platform.fight.pojo.User;

public class UserHolder {
    public static final ThreadLocal<User> holder = new ThreadLocal<>();
}
