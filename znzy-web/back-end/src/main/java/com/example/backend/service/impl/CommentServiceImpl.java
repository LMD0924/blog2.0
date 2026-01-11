package com.example.backend.service.impl;

import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    CommentMapper commentMapper;
    @Resource
    UserMapper userMapper;
    @Override
    public boolean addComment(Comment comment) {
        User user = userMapper.getUserById(comment.getUserId()); // 获取登录用户信息
        if (user != null) {
            comment.setAccount(user.getAccount()); // 设置用户名
            comment.setAvatar(user.getAvatar()); // 设置头像
        }
        comment.setTime(new java.util.Date());
        commentMapper.insertComment(comment);
        return true;
    }
    @Override
    public List<Comment> getCommentsByArticleId(Integer articleId){
        return commentMapper.getCommentsByArticleId(articleId);
    }
    @Override
    public boolean deleteComment(Integer id,Integer userId){
        commentMapper.deleteComment(id,userId);
        return true;
    }
}
