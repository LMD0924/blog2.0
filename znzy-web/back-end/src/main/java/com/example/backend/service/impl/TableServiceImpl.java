package com.example.backend.service.impl;

import com.example.backend.entity.Table;
import com.example.backend.mapper.TableMapper;
import com.example.backend.service.TableService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableServiceImpl implements TableService {
    @Resource
    private TableMapper tableMapper;

    @Override
    public int insertTable(Table table) {
        return tableMapper.insertTable(table);
    }

    @Override
    public List<Table> selectAllTables() {
        return tableMapper.selectAllTables();
    }

    @Override
    public int updateTable(Table table) {
        return tableMapper.updateTable(table);
    }

    @Override
    public int deleteTable(int id) {
        return tableMapper.deleteTable(id);
    }
}