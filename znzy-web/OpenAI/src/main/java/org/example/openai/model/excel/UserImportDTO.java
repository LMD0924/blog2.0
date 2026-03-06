package org.example.openai.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2025/12/6
 * @Description:
 */

/**
 * 用户Excel导入数据传输对象
 * <p>
 * 用于接收从Excel文件导入的用户数据，通过EasyExcel的@ExcelProperty注解与Excel列进行映射
 * </p>
 * @author 总会落叶
 * @date 2025/12/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportDTO {
    /**
     * 用户名
     * <p>Excel列头：用户名</p>
     */
    @ExcelProperty("用户名")//要与excel的表头一样
    private String username;
    
    /**
     * 手机号
     * <p>Excel列头：手机号</p>
     */
    @ExcelProperty("手机号")
    private String phone;
    
    /**
     * 邮箱
     * <p>Excel列头：邮箱</p>
     */
    @ExcelProperty("邮箱")
    private String email;

    /**
     * 部门
     * <p>Excel列头：部门</p>
     */
    @ExcelProperty("部门")
    private String department;

    /**
     * 职位
     * <p>Excel列头：职位</p>
     */
    @ExcelProperty("职位")
    private String position;

    @ExcelProperty("排名")
    private String rank;
    @ExcelProperty("企业名称")
    private String companyName;
    @ExcelProperty("营业收入（万元）")
    private String revenue;
}
