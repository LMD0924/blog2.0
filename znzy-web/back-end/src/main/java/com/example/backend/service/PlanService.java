package com.example.backend.service;


import com.example.backend.entity.Plan;

import java.util.Date;
import java.util.List;

public interface PlanService {
    int insertPlan(int userId, Date time, String content,String title, String finish, String priority);
    List<Plan> selectPlanByUserId(int userId);
    int UpdatePlan(int userId, Integer id,String title,String content,String finish,String priority);
    int deletePlan(int userId, Integer id);
}
