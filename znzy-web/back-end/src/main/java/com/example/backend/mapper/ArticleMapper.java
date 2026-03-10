package com.example.backend.mapper;

import com.example.backend.entity.Article;
import com.example.backend.entity.ArticleInfo;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface ArticleMapper {
    //发布文章
    @Insert("insert into article(authorId, title, content, time, ispublic) " +
            "values (#{authorId}, #{title}, #{content}, #{time}, #{ispublic})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertArticle(Article article);

    @Insert("insert into article_info(articleId, authorId, tag, classification, create_time) " +
            "values (#{articleId}, #{authorId}, #{tag}, #{classification}, #{createTime})")
    int insertArticleInfo(ArticleInfo articleInfo);

    // 查询所有文章（前1000条，按时间倒序）
    @Select("select id,title,authorId,likes,favorites,views,ispublic,time,substring(content,1,120) as content " +
            "from article order by time desc limit 1000")
    List<Article> getAllArticles();
    //根据id查询文章
    @Select("select * from article where id=#{id}")
    Article getArticleById(int id);
    // 更新文章主体
    @Update("UPDATE article SET title=#{title}, content=#{content}, time=#{time}, ispublic=#{ispublic} " +
            "WHERE id=#{id} AND authorId=#{authorId}")
    int updateArticleCore(int id, int authorId, String title, String content, Date time, Boolean ispublic);

    // 更新文章扩展信息（标签/分类）
    @Update("UPDATE article_info SET tag=#{tag}, classification=#{classification} " +
            "WHERE articleId=#{articleId} AND authorId=#{authorId}")
    int updateArticleInfo(int articleId, int authorId, String tag, String classification);
    
    // 根据文章ID获取文章扩展信息
    @Select("SELECT * FROM article_info WHERE articleId=#{articleId}")
    ArticleInfo getArticleInfoById(int articleId);

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

    // 文章名模糊查询
    @Select("SELECT id, title, authorId, likes, favorites, views, ispublic, time, SUBSTRING(content, 1, 120) as content " +
            "FROM article " +
            "WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY time DESC LIMIT 1000")
    List<Article> searchArticlesByTitle(@Param("keyword") String keyword);
}
