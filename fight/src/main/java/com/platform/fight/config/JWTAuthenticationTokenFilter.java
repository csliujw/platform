package com.platform.fight.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.fight.mapper.UserMapper;
import com.platform.fight.pojo.User;
import com.platform.fight.service.utils.UserDetailServiceImpl;
import com.platform.fight.service.utils.UserDetailsImpl;
import com.platform.fight.utils.JWTUtil;
import com.platform.fight.utils.RedisKeyUtils;
import io.jsonwebtoken.Claims;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("all")
// JWR 授权过滤器，拦截所有请求。
public class JWTAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Override
    // JWT 验证需要判断 token 中的用户是否可用。
    // SpringSeurity 验证也要判断用户是否可用。
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JWT token");
        String token = request.getHeader("Authorization");

        // 如果没有 token 说明未登录，则放行，让他登录。
        if (!StringUtils.hasText(token) || !token.startsWith("voucher")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);

        String username;
        try {
            Claims claims = JWTUtil.parseJWT(token);
            username = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 此处拦截到的都是用户登录成功后的请求，此时需要从 redis 中查询出数据，验证用户是否登录。
        User user = userDetailService.getUser(username);
        if (user == null) {
            throw new RuntimeException("用户名未登录");
        }
        // 用 security 验证用户是否合法。
        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}