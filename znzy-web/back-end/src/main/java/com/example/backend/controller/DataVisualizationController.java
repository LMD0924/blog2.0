package com.example.backend.controller;

import com.example.backend.DTO.CategoryCountDTO;
import com.example.backend.entity.RestBean;
import com.example.backend.service.DataVisualizationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2025/11/26
 * @Description:
 */
@RestController
@RequestMapping("api/dataVisualization")
public class DataVisualizationController {
    @Resource
    DataVisualizationService dataVisualizationService;
    @RequestMapping("/getArticleCount")
    public RestBean<Integer> getArticleCount(HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int articleCount=dataVisualizationService.getArticleCount(userId);
        if(articleCount<0) return RestBean.failure(404, "获取文章数量失败");
        return RestBean.success("获取成功",articleCount);
    }
    @RequestMapping("/getViewsCount")
    public RestBean<Integer> getViewsCount(HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int viewsCount=dataVisualizationService.getViewsCount(userId);
        if(viewsCount<0) return RestBean.failure(404,"获取失败");
        return RestBean.success("获取成功",viewsCount);
    }
    @RequestMapping("/getLikesCount")
    public RestBean<Integer> getLikesCount(HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int likesCount=dataVisualizationService.getLikesCount(userId);
        if(likesCount<0) return RestBean.failure(404,"获取失败");
        return RestBean.success("获取成功",likesCount);
    }
    @RequestMapping("/getCommentCount")
    public RestBean<Integer> getCommentCount(HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int commentCount=dataVisualizationService.getCommentCount(userId);
        if(commentCount<0) return RestBean.failure(404,"获取失败");
        return RestBean.success("获取成功",commentCount);
    }
    @RequestMapping("/getFavoritesCount")
    public RestBean<Integer> getFavoritesCount(HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int favoritesCount=dataVisualizationService.getFavoritesCount(userId);
        if(favoritesCount<0) return RestBean.failure(404,"获取失败");
        return RestBean.success("获取成功",favoritesCount);
    }
    @RequestMapping("/getCategoryCount")
    public RestBean<List<CategoryCountDTO>> getCategoryCount(HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        List<CategoryCountDTO> categoryCountDTOList=dataVisualizationService.getCategoryCount(userId);
        if(categoryCountDTOList==null) return RestBean.failure(404,"获取失败");
        return RestBean.success("获取成功",categoryCountDTOList);
    }
}
