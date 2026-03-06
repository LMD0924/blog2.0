package org.example.openai.service.excel;

import com.alibaba.excel.EasyExcel;  // 导入EasyExcel核心类，提供Excel读取功能
import org.example.openai.listener.UserExcelListener;  // 导入自定义的Excel监听器
import org.example.openai.model.excel.ExcelImportResult;  // 导入Excel导入结果封装类
import org.example.openai.model.excel.UserImportDTO;  // 导入用户导入数据转换对象
import org.springframework.stereotype.Service;  // 导入Spring服务注解
import org.springframework.web.multipart.MultipartFile;  // 导入Spring文件上传类

import java.io.InputStream;  // 导入Java输入流接口
import java.util.ArrayList;  // 导入ArrayList列表类
import java.util.Arrays;  // 导入数组工具类
import java.util.LinkedHashMap;  // 导入有序HashMap类
import java.util.List;  // 导入List接口
import java.util.Map;  // 导入Map接口

/**
 * EasyExcel解析服务类
 * 使用阿里巴巴的EasyExcel框架处理Excel文件解析
 * 相比传统POI方式，具有更高的性能和更低的内存占用
 * 主要用于用户数据的Excel导入功能
 *
 * @Author: 总会落叶
 * @Date: 2025/12/6
 */
@Service  // Spring注解，标记为服务层组件，会被Spring容器扫描并管理
public class EasyExcelService {

    /**
     * 使用EasyExcel解析用户Excel文件
     * 将Excel中的用户数据转换为统一的导入结果格式
     *
     * @param file 上传的Excel文件，必须是有效的.xlsx或.xls格式
     * @return ExcelImportResult对象，包含解析成功的数据和表头信息，或错误信息
     *         如果解析成功，返回success状态并包含数据列表和表头
     *         如果解析失败，返回error状态并包含错误消息
     */
    public ExcelImportResult<Map<String, Object>> parseWithEasyExcel(MultipartFile file) {
        // 使用try-with-resources语句，确保输入流在使用后被正确关闭，防止资源泄漏
        try (InputStream inputStream = file.getInputStream()) {
            // 创建自定义的Excel监听器实例，用于在读取Excel时处理每一行数据
            UserExcelListener listener = new UserExcelListener();

            // 使用EasyExcel构建Excel读取器链式调用：
            // 1. read()：创建读取器，指定输入流、数据模型类和监听器
            // 2. sheet()：读取第一个工作表（默认sheet0）
            // 3. headRowNumber(1)：设置表头所在行号（第1行，0-based索引）
            // 4. doRead()：开始执行读取操作
            EasyExcel.read(inputStream, UserImportDTO.class, listener)
                    .sheet()  // 指定要读取的工作表，默认读取第一个sheet
                    .headRowNumber(1)  // 设置表头行号为第1行（跳过表头行）
                    .doRead();  // 执行读取操作，开始逐行读取数据

            // 从监听器中获取解析后的数据列表
            List<UserImportDTO> userData = listener.getDataList();

            // 将UserImportDTO对象列表转换为Map<String, Object>列表，使用中文字段名作为key
            // 创建空的数据列表，用于存储转换后的数据
            List<Map<String, Object>> data = new ArrayList<>();

            // 遍历解析得到的UserImportDTO对象列表
            for (UserImportDTO user : userData) {
                // 使用LinkedHashMap保持字段顺序，确保数据顺序与表头一致
                Map<String, Object> row = new LinkedHashMap<>();

                // 将DTO对象的每个属性放入Map中，使用中文key便于前端展示
                row.put("用户名", user.getUsername());  // 添加用户名字段
                row.put("手机号", user.getPhone());    // 添加手机号字段
                row.put("邮箱", user.getEmail());      // 添加邮箱字段
                row.put("部门", user.getDepartment()); // 添加部门字段
                row.put("职位", user.getPosition());   // 添加职位字段

                // 将当前行的Map添加到数据列表中
                data.add(row);
            }

            // 定义表头列表，与Map中的key对应，用于前端展示列名
            List<String> headers = Arrays.asList("用户名", "手机号", "邮箱", "部门", "职位");

            // 调用ExcelImportResult的成功静态方法，返回成功结果
            // 参数1：转换后的数据列表
            // 参数2：表头列表
            return ExcelImportResult.success(data, headers);

        } catch (Exception e) {  // 捕获所有可能的异常
            // 如果解析过程中发生任何异常，返回错误结果
            // 使用异常的错误信息作为返回消息
            return ExcelImportResult.error(e.getMessage());
        }
        // try-with-resources会自动关闭inputStream，无需手动关闭
    }
}
