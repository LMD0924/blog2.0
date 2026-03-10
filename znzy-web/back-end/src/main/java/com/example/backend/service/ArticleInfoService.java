package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.ArticleInfo;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/3/9
 * @Description:
 */
public interface ArticleInfoService extends IService<ArticleInfo> {

    //根据文章id查询
    ArticleInfo getArticleById(Integer articleId);

    //查询所有文章信息
    List<ArticleInfo> getAllArticleInfo();

    //根据文章id修改信息
    int updateArticleById(ArticleInfo articleInfo);
}
