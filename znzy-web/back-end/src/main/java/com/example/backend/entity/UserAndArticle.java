package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserAndArticle {
    private User user;
    private Article article;
    private Integer id,likes,favorites,views;
    private String title, content,AuthorName;
    private Boolean ispublic;
    private Date time;
    private Integer isLike; // 标记是否点赞
    private Integer isFavorite; // 新增标记是否收藏
    public UserAndArticle(User user, Integer id,String title,String content,Integer likes,Integer favorites,Integer views,Boolean ispublic) {
        this.user = user;
        this.id = id;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.favorites = favorites;
        this.views = views;
        this.ispublic=ispublic;
    }
}
