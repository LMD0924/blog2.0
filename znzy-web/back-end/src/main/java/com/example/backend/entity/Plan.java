package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Plan {
    private Integer id,userId;
    private String content,finish,priority,title;
    private Date time;
}
