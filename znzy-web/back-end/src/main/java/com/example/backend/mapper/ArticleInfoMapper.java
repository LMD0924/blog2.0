package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ArticleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/*
 * @Author:总会落叶
 * @Date:2026/3/9
 * @Description:
 */
@Mapper
public interface ArticleInfoMapper extends BaseMapper<ArticleInfo> {

    //根据文章id查询信息
    @Select("select * from article_info where article_id=#{articleId}")
    ArticleInfo getArticleInfoByArticleId(int articleId);


    //根据文章id修改文章信息
    @Update("update article_info set tag=#{tag},classification=#{classification} where article_id=#{articleId}")
    int updateArticleInfo(ArticleInfo articleInfo);
}
