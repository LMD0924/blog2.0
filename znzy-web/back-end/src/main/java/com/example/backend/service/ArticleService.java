package com.example.backend.service;

import com.example.backend.entity.Article;
import com.example.backend.entity.UserAndArticle;

import java.util.Date;
import java.util.List;

public interface ArticleService {
    int InsertArticle(Article article);
    List<UserAndArticle> getAllArticles();
    Article getArticleById(Integer userId,Integer id);
    //增加文章的点赞数
    int incrementArticleLikes(Integer id);
    //记录用户对某篇文章的点赞
    int addLike(Integer userId, Integer articleId, Date time);
    //检查用户是否点赞了某篇文章
    int checkLike(Integer userId,Integer articleId);
    //减少文章的点赞数
    int decrementArticleLikes(Integer id);

    int deleteLike(Integer userId,Integer articleId);
    //增加文章的收藏数
    int incrementArticleFavorites(Integer id);
    //记录用户对某篇文章的收藏
    int addFavorite(Integer userId,Integer articleId,Date time);
    //检查用户是否收藏了某篇文章
    int  checkFavorite(Integer userId,Integer articleId);
    //减少文章的收藏
    int decrementArticleFavorites(Integer id);
    int deleteFavorite(Integer userId,Integer articleId);
    //更新文章
    int updateArticle(Integer id,Integer authorId,String title,String content,Date time,String tag,String category,Boolean ispublic);
    //删除文章
    int deleteArticle(Integer id,Integer authorId);
    List<Article> getArticlesByUserId(int userId);
    //获取用户的收藏和点赞
    List<Article> getLikedArticlesByUserId(int userId);
    List<Article> getFavoriteArticlesByUserId(int userId);
    //获取浏览量前五条
    List<Article> getTopArticles();
}
