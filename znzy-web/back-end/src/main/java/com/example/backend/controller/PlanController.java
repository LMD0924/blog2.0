package com.example.backend.controller;


import com.example.backend.annot.Log;
import com.example.backend.entity.Plan;
import com.example.backend.entity.RestBean;
import com.example.backend.service.PlanService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping ("/api/plan")
public class PlanController {
    @Resource
    PlanService planService;

    @PostMapping("/AddPlan")
    public RestBean<String> addPlan(@RequestParam("content") String content,
                                    @RequestParam("title") String title,
                                    @RequestParam("finish") String finish,
                                    @RequestParam("priority") String priority,
                                    HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id");
        int status=planService.insertPlan(userId, new Date(),content,title,finish,priority);
        if (status > 0) {
            return RestBean.success("添加计划成功");
        } else {
            return RestBean.failure(401,"添加计划失败");
        }
    }
        @Log
        @PostMapping("UpdatePlan")
    public RestBean<Integer> UpdatePlan(@RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("finish") String finish,
                                        @RequestParam("priority") String priority,
                                        @RequestParam("id") Integer id,
                                        HttpServletRequest request) {
            Integer userId = (Integer) request.getAttribute("id");
            int status = planService.UpdatePlan(userId, id, title, content, finish, priority);
            if (status > 0) {
                return RestBean.success("修改成功", status);
            } else {
                return RestBean.failure(401, "修改失败");
            }
        }
    @GetMapping("AllPlan")
    public RestBean<List<Plan>> getAllPlan(HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id");
        return RestBean.success("cg",planService.selectPlanByUserId(userId));
    }
    @PostMapping("/delete")
            public RestBean<String> deletePlan(@RequestParam("id") int id,
                                               HttpServletRequest request) {
                int userId = (Integer) request.getAttribute("id");
                int status = planService.deletePlan( userId,id);
                if (status > 0) {
                    return RestBean.success("删除成功");
                }
                else {
                    return RestBean.failure(401,"删除失败");
                }
            }
    }
