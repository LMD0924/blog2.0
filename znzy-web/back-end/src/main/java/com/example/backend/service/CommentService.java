package com.example.backend.service;

import com.example.backend.entity.Comment;

import java.util.List;

public interface CommentService {
    boolean addComment(Comment comment);
    List<Comment> getCommentsByArticleId(Integer articleId);
    boolean deleteComment(Integer id,Integer userId);
}
