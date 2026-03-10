package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.ArticleInfo;
import com.example.backend.mapper.ArticleInfoMapper;
import com.example.backend.service.ArticleInfoService;
import com.example.backend.util.UpdateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/3/9
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleInfoServiceImpl extends ServiceImpl<ArticleInfoMapper, ArticleInfo> implements ArticleInfoService {

    private final ArticleInfoMapper articleInfoMapper;

    //根据文章id查询文章信息
    @Override
    public ArticleInfo getArticleById(Integer articleId) {
        return articleInfoMapper.getArticleInfoByArticleId(articleId);
    }

    //查询所有文章信息
    @Override
    public List<ArticleInfo> getAllArticleInfo() {
        return articleInfoMapper.selectList(null);
    }

    //根据id修改信息
    @Override
    public int updateArticleById(ArticleInfo articleInfo) {
        ArticleInfo old = articleInfoMapper.getArticleInfoByArticleId(articleInfo.getArticleId());
        log.info("旧的数据id为：{}",old);
        if(old==null) return 0;
        UpdateUtil.copyNonNullProperties(articleInfo, old);
        log.info("新的数据：{}",old);
        return articleInfoMapper.updateArticleInfo(old);
    }

}
