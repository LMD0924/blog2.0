package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.Table;
import com.example.backend.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table")
public class TableController {
    @Autowired
    private TableService tableService;

    @PostMapping("/add")
    public RestBean<String> addTable(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("title") String title) {
        Table table = new Table();
        table.setName(name);
        table.setEmail(email);
        table.setTitle(title);
        int status = tableService.insertTable(table);
        if (status > 0) {
            return RestBean.success("添加成功");
        } else {
            return RestBean.failure(400, "添加失败");
        }
    }

    @GetMapping("/all")
    public RestBean<List<Table>> getAllTables() {
        List<Table> tables = tableService.selectAllTables();
        return RestBean.success("获取成功", tables);
    }

    @PutMapping("/update/{id}")
    public RestBean<String> updateTable(
            @PathVariable int id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("title") String title) {
        Table table = new Table();
        table.setId(id);
        table.setName(name);
        table.setEmail(email);
        table.setTitle(title);
        int status = tableService.updateTable(table);
        if (status > 0) {
            return RestBean.success("更新成功");
        } else {
            return RestBean.failure(400, "更新失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public RestBean<String> deleteTable(@PathVariable int id) {
        int status = tableService.deleteTable(id);
        if (status > 0) {
            return RestBean.success("删除成功");
        } else {
            return RestBean.failure(400, "删除失败");
        }
    }
}