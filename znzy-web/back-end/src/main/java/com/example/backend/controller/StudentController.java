package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.Student;
import com.example.backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/Student")
public class StudentController {
    @Autowired
    private StudentService studentService;
    @PostMapping("/add")
    public RestBean<String> addStudent(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("score") Float score,
            @RequestParam("course") String course,
            @RequestParam("sid") String sid){
        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setScore(score);
        student.setCourse(course);
        student.setSid(sid);
        int status=studentService.insertStudent(student);
        if(status>0) {
            return RestBean.success("添加成功");
        }else {
            return RestBean.failure(400, "添加失败");
        }
    }
    @GetMapping("/all")
    public RestBean<List<Student>> getAllStudent() {
        List<Student> students=studentService.selectAllStudents();
        return RestBean.success("获取成功",students);
    }
    @PutMapping("/update/{id}")
    public RestBean<String> updateStudent(
            @PathVariable int id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("score") Float score,
            @RequestParam("course") String course,
            @RequestParam("sid") String sid){
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setEmail(email);
        student.setScore(score);
        student.setCourse(course);
        student.setSid(sid);
        int status = studentService.updateStudent(student);
        if(status>0) {
            return RestBean.success("更新成功");
        }else {
            return RestBean.failure(400,"更新失败");
        }
    }
    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteStudent(@PathVariable int id) {
        int status = studentService.deleteStudent(id);
        if(status>0){
            return RestBean.success("删除成功");
        }else {
            return RestBean.failure(400,"删除失败");
        }
    }
}
