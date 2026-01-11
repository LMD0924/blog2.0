package com.example.backend.controller;

import com.example.backend.entity.Comment;
import com.example.backend.entity.RestBean;
import com.example.backend.service.CommentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    // 添加评论
    @PostMapping("addComment")
    public RestBean<String> addComment(
            @RequestParam String content, // 评论内容
            @RequestParam Integer articleId, // 文章 ID
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("id"); // 获取当前用户 ID
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(articleId);
        comment.setUserId(userId);

        if (commentService.addComment(comment)) {
            return RestBean.success("评论成功！");
        } else {
            return RestBean.failure(500, "评论失败");
        }
    }

    // 获取某篇文章的评论
    @GetMapping("/article")
    public RestBean<List<Comment>> getCommentsByArticleId(@RequestParam Integer articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return RestBean.success("获取成功！", comments);
    }

    // 删除评论
    @DeleteMapping("/delete")
    public RestBean<String> deleteComment(@RequestParam Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("id"); // 获取当前用户 ID
        if (commentService.deleteComment(id, userId)) {
            return RestBean.success("评论删除成功！");
        } else {
            return RestBean.failure(403, "无权删除此评论");
        }
    }
}