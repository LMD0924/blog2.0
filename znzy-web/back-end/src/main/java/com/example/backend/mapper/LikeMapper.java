package com.example.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface LikeMapper {
    @Insert("insert into `like-re`(userId,articleId,time) values (#{userId},#{articleId},#{time})")
    int addLike(Integer userId, Integer articleId, Date time);
    @Select("select id from `like-re` where userId=#{userId} and articleId=#{articleId}")
    Integer checkLike(Integer userId,Integer articleId);
    @Delete("delete from `like-re` where userId=#{userId} and articleId=#{articleId}")
    int deleteLike(Integer userId,Integer articleId);
}
