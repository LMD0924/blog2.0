package com.example.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface FollowMapper {
    //关注他人
    @Insert("insert ignore into follow (userId,followId,time) values (#{userId},#{followId},#{time})")
    int InsertFollow(Integer userId, Integer followId, Date time);
    //取消关注他人
    @Delete("delete from follow where userId=#{userId} and followId=#{followId}")
    int DeleteFollow(Integer userId,Integer followId);
    //检查是否关注
    @Select("select count(*) from follow where userId=#{userId} and followId=#{followId}")
    int CheckFollow(Integer userId,Integer followId);
}
