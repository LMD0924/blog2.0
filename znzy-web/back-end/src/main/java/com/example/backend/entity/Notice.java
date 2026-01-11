package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/*
 * @Author:总会落叶
 * @Date:2025/10/28
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    private Integer id,userId;
    private String title,content,type,priority,receive;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date time;
}
