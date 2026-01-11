package com.example.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface FavoriteMapper {
    @Insert("insert into `favorite-re` (userId,articleId,time) values (#{userId},#{articleId},#{time})")
    int addFavorite(Integer userId, Integer articleId, Date time);
    @Select("select id from `favorite-re` where userId=#{userId} and articleId=#{articleId}")
    Integer checkFavorite(Integer userId,Integer articleId);
    @Delete("delete from `favorite-re` where userId=#{userId} and articleId=#{articleId}")
    int deleteFavorite(Integer userId,Integer articleId);
}
