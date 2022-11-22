package com.platform.fight.service.interfaces;

import java.util.Map;

public interface IUserService {
    public abstract Map<String, String> login(String username, String password);

    public abstract Map<String, String> getInfo();

    public abstract Map<String, String> register(String username, String password, String confirmedPassword, String photo);
}
