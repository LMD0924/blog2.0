package com.example.backend.service.impl;

import com.example.backend.entity.Article;
import com.example.backend.entity.ArticleInfo;
import com.example.backend.entity.User;
import com.example.backend.entity.UserAndArticle;
import com.example.backend.mapper.ArticleMapper;
import com.example.backend.mapper.FavoriteMapper;
import com.example.backend.mapper.LikeMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Resource
    ArticleMapper articleMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    LikeMapper likeMapper;
    @Resource
    FavoriteMapper favoriteMapper;

    //增加文章
    @Override
    @Transactional
    public int InsertArticle(Article article, ArticleInfo articleInfo){
        int result = articleMapper.insertArticle(article);
        if(result<=0) return 0;
        // 设置文章ID到文章信息中
        articleInfo.setArticleId(article.getId());
        return articleMapper.insertArticleInfo(articleInfo);
    }
    @Override
    public List<UserAndArticle> getAllArticles() {
        List<UserAndArticle> AllArticles=new ArrayList<>();
        for(Article article : articleMapper.getAllArticles()){
            User user=userMapper.getUserById(article.getAuthorId());
            AllArticles.add(new UserAndArticle(user,article.getId(),article.getTitle(),article.getContent(),article.getLikes(),article.getFavorites(),article.getViews(),article.getIspublic()));
        }
        return AllArticles;
    }

    @Override
    public Article getArticleById(Integer userId, Integer id) {
        int a=articleMapper.incrementArticleViews(id);
        Integer answer=likeMapper.checkLike(userId,id);
        Article article=articleMapper.getArticleById(id);
        if (answer==null)article.setIsLike(0);
        else article.setIsLike(1);
        return article;
    }
    //增加文章的点赞数
    @Override
    public int incrementArticleLikes(Integer id){
        return articleMapper.incrementArticleLikes(id);
    }
    //记录用户对某篇文章的点赞
    @Override
    public int addLike(Integer userId, Integer articleId, Date time){
        return likeMapper.addLike(userId,articleId,time);
    }
    //检查用户是否点赞了某篇文章
    @Override
    public int checkLike(Integer userId,Integer articleId){
        Integer answer=likeMapper.checkLike(userId,articleId);
        Article article=articleMapper.getArticleById(articleId);
        if(answer==null){
            article.setIsLike(0);
        }else{
            article.setIsLike(1);
        }
        if(answer!=null) return 1;
        else return 0;
    }
    //减少文章的点赞数
    @Override
    public int decrementArticleLikes(Integer id){
        return articleMapper.decrementArticleLikes(id);
    }
    //删除点赞记录
    @Override
    public int deleteLike(Integer userId,Integer articleId){
        return likeMapper.deleteLike(userId,articleId);
    }
    //增加文章的收藏数
    @Override
    public int incrementArticleFavorites(Integer id){
        return articleMapper.incrementArticleFavorites(id);
    }
    //记录用户对某篇文章的收藏
    @Override
    public int addFavorite(Integer userId,Integer articleId,Date time){
        return favoriteMapper.addFavorite(userId,articleId,time);
    }
    //检查用户是否收藏了某篇文章
    @Override
    public int checkFavorite(Integer userId,Integer articleId){
        Integer answer=favoriteMapper.checkFavorite(userId,articleId);
        Article article=articleMapper.getArticleById(articleId);
        if(answer==null){
            article.setIsFavorite(0);
        }else{
            article.setIsFavorite(1);
        }
        if(answer!=null) return 1;
        else return 0;
    }
    //取消收藏
    @Override
    public int decrementArticleFavorites(Integer id){
        return articleMapper.decrementArticleFavorites(id);
    }
    //删除收藏记录
    @Override
    public int deleteFavorite(Integer userId,Integer articleId){
        return favoriteMapper.deleteFavorite(userId,articleId);
    }
    //更新文章
    @Override
    public int updateArticle(Integer id,Integer authorId,String title,String content,Date time,String tag,String classification,Boolean ispublic){
        Article article=articleMapper.getArticleById(id);
        if(article==null||!article.getAuthorId().equals(authorId)){
            return 0;//暂无权限更改
        }
        // 更新文章主体
        int result = articleMapper.updateArticleCore(id, authorId, title, content, time, ispublic);
        if(result<=0) return 0;
        // 更新文章扩展信息
        return articleMapper.updateArticleInfo(id, authorId, tag, classification);
    }
    //删除文章
    @Override
    public int deleteArticle(Integer id,Integer authorId){
        Article article=articleMapper.getArticleById(id);
        if(article==null||!article.getAuthorId().equals(authorId)){
            return 0;//暂无权限更改
        }
        return articleMapper.deleteArticle(id,authorId);
    }
    @Override
    public List<Article> getArticlesByUserId(int userId) {
        return articleMapper.getArticlesByUserId(userId);
    }
    //获取用户的收藏和点赞
    @Override
    public List<Article> getLikedArticlesByUserId(int userId){
        return articleMapper.findLikedArticlesByUserId(userId);
    }
    @Override
    public List<Article> getFavoriteArticlesByUserId(int userId){
        return articleMapper.findFavoriteArticlesByUserId(userId);
    }
    //获取文章浏览量前五条
    @Override
    public List<Article> getTopArticles(){
        return articleMapper.getTopArticles();
    }

    // 文章搜索
    @Override
    public List<Article> searchArticlesByTitle(String keyword) {
        return articleMapper.searchArticlesByTitle(keyword);
    }
}
