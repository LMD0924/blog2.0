package com.example.backend.mapper;

import com.example.backend.DTO.CategoryCountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2025/11/26
 * @Description:
 */
@Mapper
public interface DataVisualizationMapper {
    //获取总的文章数
    @Select("select count(*) from article where authorId=#{authorId}")
    int getArticleCount(Integer authorId);
    //获取总的浏览量
    @Select("select sum(views) from article where authorId=#{authorId}")
    int getViewsCount(Integer authorId);
    //获取总的点赞数
    @Select("select sum(likes) from article where authorId=#{authorId}")
    int getLikesCount(Integer authorId);
    //获取总的评论数
    @Select("select a.authorId,count(c.id) as commentCount from article a left join comment c on a.id=c.articleId where authorId=#{authorId} group by a.authorId")
    int getCommentCount(Integer authorId);
    //获取总的收藏数
    @Select("select sum(favorites) from article where authorId=#{authorId}")
    int getFavoritesCount(Integer authorId);
    //获取文章的分类
    @Select("select category,count(*) as count from article where authorId=#{authorId} group by category")
    List<CategoryCountDTO> getCategoryCount(Integer authorId);
}
