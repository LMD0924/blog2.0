package com.example.backend.mapper;

import com.example.backend.entity.Table;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TableMapper {
    @Insert("INSERT INTO `table`(name, email, title) VALUES (#{name}, #{email}, #{title})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTable(Table table);

    @Select("SELECT * FROM `table`")
    List<Table> selectAllTables();

    @Update("UPDATE `table` SET name = #{name}, email = #{email}, title = #{title} WHERE id = #{id}")
    int updateTable(Table table);

    @Delete("DELETE FROM `table` WHERE id = #{id}")
    int deleteTable(int id);
}