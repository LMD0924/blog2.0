package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor // 添加无参构造函数
public class User {
    private Integer id,follow,fans;
    private String name,password,avatar,email,account,role,status;
}
