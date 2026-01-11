package com.example.backend.service.impl;

import com.example.backend.entity.Student;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    @Resource
    private StudentMapper studentMapper;
    @Override
    public int insertStudent(Student student) {
        return studentMapper.insertStudent(student);
    }

    @Override
    public List<Student> selectAllStudents() {
        return studentMapper.selectAllStudents();
    }

    @Override
    public int updateStudent(Student student) {
        return studentMapper.updateStudent(student);
    }

    @Override
    public int deleteStudent(int id) {
        return studentMapper.deleteStudent(id);
    }
}
