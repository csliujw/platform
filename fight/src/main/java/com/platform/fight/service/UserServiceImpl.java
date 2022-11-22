package com.platform.fight.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.fight.mapper.UserMapper;
import com.platform.fight.pojo.User;
import com.platform.fight.service.interfaces.IUserService;
import com.platform.fight.service.utils.UserDetailsImpl;
import com.platform.fight.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("all")
public class UserServiceImpl implements IUserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public User selectUserById() {
        return null;
    }

    @Override
    public Map<String, String> login(String username, String password) {
        // 这样就会对密码进行加密了。用户登录时调用的是 UserDetailServiceImple#loadUserByUsername 方法。使用它对用户进行登录。
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        // 验证是否可以正常登录。如果登录失败会自动处理
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        // 拿到用户信息
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User retVal = loginUser.getUser();
        // 使用 username 生成 JWT token,
        String jwt = JWTUtil.createJWT(username, retVal.getId());

        HashMap<String, String> map = new HashMap<>();
        map.put("resp_message", "success");
        map.put("token", jwt);
        return map;
    }

    @Override
    public Map<String, String> getInfo() {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl logUser = (UserDetailsImpl) token.getPrincipal();
        User user = logUser.getUser();
        HashMap<String, String> map = new HashMap<>();
        map.put("resp_message", "success");
        map.put("id", user.getId().toString());
        map.put("username", user.getUsername());
        map.put("photo", user.getPhoto());
        return map;
    }

    @Override
    public Map<String, String> register(String username, String password, String confirmPassword, String photo) {
        HashMap<String, String> map = new HashMap<>();
        if (username == null || password == null) {
            map.put("resp_message", "用户名/密码不能为空");
            return map;
        }
        username = username.trim();
        password = password.trim();

        if (username.length() == 0 || password.length() == 0) {
            map.put("resp_message", "用户名/密码不能为空");
            return map;
        }

        if (username.length() > 100 || password.length() > 100) {
            map.put("resp_message", "用户名/密码长度不能大于100");
            return map;
        }

        if (!password.equals(confirmPassword)) {
            map.put("resp_message", "两次密码不一致");
            return map;
        }
        if (password.length() != password.trim().length()) {
            map.put("resp_message", "密码不能包含空格");
            return map;
        }
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username);
        User user = mapper.selectOne(query);
        if (user != null) {
            map.put("resp_message", "用户名已存在");
            return map;
        }
        if (photo == null || "".equals(photo.trim())) {
            photo = "https://cdn.acwing.com/media/user/profile/photo/36348_lg_ed620b9880.jpg";
        }
        mapper.insert(new User(null, username, passwordEncoder.encode(password), photo, 1000, 0));
        map.put("resp_message", "注冊成功");
        return map;
    }
}
