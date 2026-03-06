package com.example.backend.DTO;

import com.example.backend.entity.Article;
import com.example.backend.entity.ArticleInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/6
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {

    private Article article;
    private ArticleInfo articleInfo;
}
