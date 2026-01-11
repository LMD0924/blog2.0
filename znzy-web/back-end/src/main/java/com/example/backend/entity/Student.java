package com.example.backend.entity;

import lombok.Data;

@Data
public class Student {
    private Integer id;
    private String sid;
    private String name;
    private String email;
    private Float score;
    private String course;
}
