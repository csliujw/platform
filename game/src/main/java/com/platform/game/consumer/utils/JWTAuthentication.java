package com.platform.game.consumer.utils;

import com.platform.fight.utils.JWTUtil;
import io.jsonwebtoken.Claims;

// 解析 token 中的数据
public class JWTAuthentication {
    public static Integer getUserId(String token) {
        int userId = -1;
        try {
            Claims claims = JWTUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getIssuer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return userId;
    }

    public static String getUserName(String token) {
        String subject = null;
        try {
            Claims claims = JWTUtil.parseJWT(token);
            subject = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subject;
    }
}
