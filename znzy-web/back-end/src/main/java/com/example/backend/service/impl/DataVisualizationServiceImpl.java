package com.example.backend.service.impl;

import com.example.backend.DTO.CategoryCountDTO;
import com.example.backend.mapper.DataVisualizationMapper;
import com.example.backend.service.DataVisualizationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2025/11/26
 * @Description:
 */
@Service
public class DataVisualizationServiceImpl implements DataVisualizationService {
    @Resource
    DataVisualizationMapper dataVisualizationMapper;
    @Override
    public int getArticleCount(Integer authorId) {
        return dataVisualizationMapper.getArticleCount(authorId);
    }

    @Override
    public int getViewsCount(Integer authorId) {
        return dataVisualizationMapper.getViewsCount(authorId);
    }

    @Override
    public int getLikesCount(Integer authorId) {
        return dataVisualizationMapper.getLikesCount(authorId);
    }

    @Override
    public int getCommentCount(Integer authorId) {
        return dataVisualizationMapper.getCommentCount(authorId);
    }

    @Override
    public int getFavoritesCount(Integer authorId) {
        return dataVisualizationMapper.getFavoritesCount(authorId);
    }

    @Override
    public List<CategoryCountDTO> getCategoryCount(Integer authorId) {
        return dataVisualizationMapper.getCategoryCount(authorId);
    }
}
