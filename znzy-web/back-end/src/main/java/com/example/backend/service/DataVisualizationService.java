package com.example.backend.service;

import com.example.backend.DTO.CategoryCountDTO;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2025/11/26
 * @Description:
 */
public interface DataVisualizationService {
    int getArticleCount(Integer authorId);
    int getViewsCount(Integer authorId);
    int getLikesCount(Integer authorId);
    int getCommentCount(Integer authorId);
    int getFavoritesCount(Integer authorId);
    List<CategoryCountDTO> getCategoryCount(Integer authorId);
}
