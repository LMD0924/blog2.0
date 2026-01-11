package com.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2025/11/28
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCountDTO {
    private String category;
    private int count;
}
