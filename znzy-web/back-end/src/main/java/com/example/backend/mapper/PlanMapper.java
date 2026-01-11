package com.example.backend.mapper;

import com.example.backend.entity.Plan;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface PlanMapper {

    @Insert("INSERT INTO plan(userId,time,content,title,finish,priority) VALUES (#{userId},#{time},#{content},#{title},#{finish},#{priority})")
    int insertPlan(int userId, Date time, String content,String title, String finish, String priority);

    //获取对应的计划
    @Select("SELECT * FROM plan WHERE userId=#{userId}")
    List<Plan> selectPlanByUserId(int userId);

    @Update("UPDATE plan SET title=#{title}, content=#{content}, finish=#{finish}, priority=#{priority} WHERE userId=#{userId} AND id=#{id}")
    int UpdatePlan(int userId, Integer id,String title,String content,String finish,String priority);

    @Delete("DELETE FROM plan WHERE userId=#{userId} AND id=#{id}")
    int deletePlan(int userId, Integer id);
}
