package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.JWTUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth/")
public class AuthController {
    @Resource
    UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public RestBean<String> login(@RequestParam("username") String name, @RequestParam("password") String password) {
        User user=userService.login(name,password);
        if(user!=null) return RestBean.success("登录成功",JWTUtil.createToken(user));
        return RestBean.failure(404,"用户名或密码错误");
    }
}