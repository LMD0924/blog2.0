package com.example.backend.mapper;

import com.example.backend.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface ArticleMapper {
    //发布文章
    @Insert("insert into article(authorId,title,content,time,category,tag,ispublic) values (#{authorId},#{title},#{content},#{time},#{category},#{tag},#{ispublic})")
    int insertArticle(Article article);
    //查询所有文章
    @Select("select id,title,authorId,likes,favorites,views,category,tag,ispublic,substring(content,1,120) as content from article order by id desc limit 50")
    List<Article> getAllArticles();
    //根据id查询文章
    @Select("select * from article where id=#{id}")
    Article getArticleById(int id);
    //更新文章
    @Update("UPDATE article set title=#{title},content=#{content},time=#{time},tag=#{tag},category=#{category},ispublic=#{ispublic} where id=#{id} and authorId=#{authorId}")
    int updateArticle(int id, int authorId, String title, String content, Date time,String tag,String category,Boolean ispublic);
    //删除文章
    @Delete("DELETE from article where id=#{id} and authorId=#{authorId}")
    int deleteArticle(int id, int authorId);
    //增加观看量
    @Update("UPDATE article SET views=views+1 where id=#{id}")
    int incrementArticleViews(int id);
    //点赞
    @Update("UPDATE article set likes=likes+1 where id=#{id}")
    int incrementArticleLikes(int id);
    //取消点赞
    @Update("UPDATE article set likes=likes-1 where id=#{id} and likes>0")
    int decrementArticleLikes(int id);
    //收藏
    @Update("UPDATE article set favorites=favorites+1 where id=#{id}")
    int incrementArticleFavorites(int id);
    //取消收藏
    @Update("UPDATE article set favorites=favorites-1 where id=#{id} and favorites>0")
    int decrementArticleFavorites(int id);
    @Select("SELECT * FROM article WHERE authorId = #{userId} ORDER BY time DESC")
    List<Article> getArticlesByUserId(int userId);
    //获取用户的点击和收藏
    @Select("SELECT a.* FROM article a " +
            "JOIN `like-re` ul ON a.id = ul.articleId " +
            "WHERE ul.userId = #{userId}")
    List<Article> findLikedArticlesByUserId(@Param("userId") int userId);

    @Select("SELECT a.* FROM article a " +
            "JOIN `favorite-re` uf ON a.id = uf.articleId " +
            "WHERE uf.userId = #{userId}")
    List<Article> findFavoriteArticlesByUserId(@Param("userId") int userId);

    //获取文章浏览量前五条
    @Select("select * from article order by views desc limit 5")
    List<Article> getTopArticles();
}
