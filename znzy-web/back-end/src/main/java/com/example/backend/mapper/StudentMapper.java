package com.example.backend.mapper;

import com.example.backend.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper {
    @Insert("INSERT INTO Student (name , email , score , course,sid) VALUES (#{name}, #{email}, #{score}, #{course},#{sid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertStudent(Student student);
    @Select("SELECT * FROM Student")
    List<Student> selectAllStudents();
    @Update("UPDATE Student SET name = #{name}, email = #{email}, score = #{score}, course = #{course},sid=#{sid} WHERE id = #{id}")
    int updateStudent(Student student);
    @Delete("DELETE FROM Student WHERE id = #{id}")
    int deleteStudent(int id);
}
