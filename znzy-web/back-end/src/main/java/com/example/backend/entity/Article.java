package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@Data
public class Article {
    private Integer id,authorId,likes,favorites,views;
    private String title, content,category,tag;
    private Boolean ispublic;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date time;
    @Setter
    private Integer IsLike,IsFavorite;

    public Article() {
        // Default constructor for Jackson deserialization
    }

    public Article(int id, String title, String content, Date date,String category,String tag,Boolean ispublic) {
        this.title = title;
        this.content = content;
        this.time = date;
        this.authorId = id;
        this.category = category;
        this.tag = tag;
        this.ispublic = ispublic;
    }
}
