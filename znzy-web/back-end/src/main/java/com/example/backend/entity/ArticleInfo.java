package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/*
 * @Author:总会落叶
 * @Date:2026/3/6
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_info")
public class ArticleInfo {

    private Integer id;
    private Integer articleId;       // 文章id
    private Integer authorId;        // 作者id
    private String tag;              // 标签（逗号分隔）
    private String classification;   // 分类
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createTime;
}
