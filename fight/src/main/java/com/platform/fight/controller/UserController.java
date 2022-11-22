package com.platform.fight.controller;

import com.platform.fight.service.RankListServiceImpl;
import com.platform.fight.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private UserServiceImpl userService;
    private RankListServiceImpl rankListService;

    // 能拿到 token 正常解析就说明可以正常登录
    @PostMapping("/account/token/")
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userService.login(username, password);
    }

    @GetMapping("/account/info/")
    public Map<String, String> getInfo() {
        // token 会被 security 解析，用于获取用户
        System.out.println("获取用户信息");
        rankListService.saveToRedis();
        return userService.getInfo();
    }

    @PostMapping("/account/register/")
    public Map<String, String> register(@RequestParam Map<String, String> map) {
        return userService.register(map.get("username"), map.get("password"), map.get("confirmPassword"), map.get("photo"));
    }
}
