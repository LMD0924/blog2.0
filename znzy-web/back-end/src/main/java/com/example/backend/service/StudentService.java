package com.example.backend.service;

import com.example.backend.entity.Student;

import java.util.List;

public interface StudentService {
    int insertStudent(Student student);
    List<Student> selectAllStudents();
    int updateStudent(Student student);
    int deleteStudent(int id);
}
