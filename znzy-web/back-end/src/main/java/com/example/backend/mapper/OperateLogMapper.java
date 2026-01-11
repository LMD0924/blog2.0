package com.example.backend.mapper;

import com.example.backend.entity.OperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/*
 * @Author:总会落叶
 * @Date:2025/12/23
 * @Description:
 */
@Mapper
public interface OperateLogMapper {
    //添加操作日志
    @Insert("insert into operate_log (userId,userName,operateTime,className,methodName,success,errorMessage,costTime,clientIp,requestUri) values(#{userId},#{userName},#{operateTime},#{className},#{methodName},#{success},#{errorMessage},#{costTime},#{clientIp},#{requestUri}))")
    Integer addOperateLog(OperateLog operateLog);
}
