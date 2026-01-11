package com.example.backend.util;

/*
 * @Author:总会落叶
 * @Date:2025/12/23
 * @Description:
 */

import com.auth0.jwt.interfaces.Claim;
import com.example.backend.entity.User;

import java.util.Map;

/**
 * 用户上下文工具类 - 使用ThreadLocal存储当前用户信息
 */
public class UserContext {

    // 使用ThreadLocal存储当前线程的用户信息
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }
    /**
     * 设置当前用户信息（通过参数）
     */
    public static void setCurrentUser(Integer id, String name, String avatar) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAvatar(avatar);
        setCurrentUser(user);
    }
    /**
     * 从JWT token设置当前用户信息
     */
    public static void setCurrentUserFromToken(String token) {
        Map<String, Claim> claims = JWTUtil.verifyToken(token);
        if (claims != null && !claims.isEmpty()) {
            User user = new User();
            user.setId(claims.get("id").asInt());
            user.setName(claims.get("name").asString());
            user.setAvatar(claims.get("avatar").asString());
            setCurrentUser(user);
        }
    }

    /**
     * 从JWT claims直接设置
     */
    public static void setCurrentUserFromClaims(Map<String, Claim> claims) {
        if (claims != null && !claims.isEmpty()) {
            User user = new User();
            user.setId(claims.get("id").asInt());
            user.setName(claims.get("name").asString());
            if (claims.containsKey("avatar")) {
                user.setAvatar(claims.get("avatar").asString());
            }
            setCurrentUser(user);
        }
    }

    /**
     * 获取当前用户信息
     */
    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUserName() {
        User user = getCurrentUser();
        return user != null ? user.getName() : null;
    }

    /**
     * 获取当前用户头像
     */
    public static String getCurrentUserAvatar() {
        User user = getCurrentUser();
        return user != null ? user.getAvatar() : null;
    }

    /**
     * 清除当前用户信息（防止内存泄漏）
     */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
