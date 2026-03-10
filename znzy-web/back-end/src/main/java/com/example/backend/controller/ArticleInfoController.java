package com.example.backend.controller;

import com.example.backend.entity.ArticleInfo;
import com.example.backend.entity.RestBean;
import com.example.backend.service.ArticleInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/3/9
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("api/articleInfo")
@RequiredArgsConstructor
public class ArticleInfoController {

    private final ArticleInfoService articleInfoService;

    //根据文章id查询文章信息
    @GetMapping("/getArticleInfoById/{articleId}")
    public RestBean<ArticleInfo> getArticleById(@PathVariable("articleId") Integer articleId){
        log.info("文章ID:{}",articleId);
        ArticleInfo articleInfo = articleInfoService.getArticleById(articleId);
        log.info("文章信息:{}",articleInfo);
        if(articleInfo != null){
            return RestBean.success("成功",articleInfo);
        }else {
            return RestBean.failure(404,"查询失败");
        }
    }

    //获取全部的信息
    @GetMapping("/getAllArticleInfo")
    public RestBean<List<ArticleInfo>> getAllArticleInfo(){
        return RestBean.success("成功",articleInfoService.getAllArticleInfo());
    }

    //根据文章id修改文章信息
    @PutMapping("/updateArticleInfo")
    public RestBean<Integer> updateArticleInfo(ArticleInfo articleInfo){
        return RestBean.success("成功",articleInfoService.updateArticleById(articleInfo));
    }

}
