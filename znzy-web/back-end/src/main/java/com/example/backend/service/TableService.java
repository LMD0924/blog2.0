package com.example.backend.service;

import com.example.backend.entity.Table;

import java.util.List;

public interface TableService {
    int insertTable(Table table);
    List<Table> selectAllTables();
    int updateTable(Table table);
    int deleteTable(int id);
}