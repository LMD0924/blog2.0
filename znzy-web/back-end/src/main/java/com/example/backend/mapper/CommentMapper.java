package com.example.backend.mapper;

import com.example.backend.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Insert("INSERT INTO comment (content, articleId, userId, time,account,avatar) VALUES (#{content}, #{articleId}, #{userId}, #{time},#{account},#{avatar})")
    void insertComment(Comment comment);

    @Select("SELECT c.*, u.account AS account, u.avatar AS avatar " +
            "FROM comment c JOIN user u ON c.userId = u.id " +
            "WHERE c.articleId = #{articleId} ORDER BY c.time DESC")
    List<Comment> getCommentsByArticleId(Integer articleId);
    @Delete("DELETE FROM comment WHERE id = #{id} AND userId = #{userId}")
    void deleteComment(Integer id, Integer userId);
}
