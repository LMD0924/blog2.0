package com.example.backend.service.impl;

import com.example.backend.entity.Plan;
import com.example.backend.mapper.PlanMapper;
import com.example.backend.service.PlanService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {
    @Resource
    PlanMapper planMapper;

    @Override
    public int insertPlan(int userId, Date time, String content,String title, String finish, String priority) {
        return planMapper.insertPlan(userId, time, content,title,finish, priority);
    }

    @Override
    public List<Plan> selectPlanByUserId(int userId) {
        return planMapper.selectPlanByUserId(userId);
    }

    @Override
    public int UpdatePlan(int userId, Integer id,String title,String content,String finish,String priority) {
        return planMapper.UpdatePlan(userId,id,title,content,finish,priority);
    }
    @Override
    public int deletePlan(int userId, Integer id){
        return planMapper.deletePlan(userId,id);
    }
}
