package org.example.openai.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.example.openai.model.excel.UserImportDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2025/12/6
 * @Description:
 */

/**
 * Excel用户数据解析监听器
 * <p>
 * 继承自EasyExcel的AnalysisEventListener，用于处理Excel文件中用户数据的解析过程
 * 包括数据校验、数据收集和错误记录
 * </p>
 */
public class UserExcelListener extends AnalysisEventListener<UserImportDTO> {
    /**
     * 存储解析成功的用户数据列表
     */
    private List<UserImportDTO> dataList=new ArrayList<>();
    
    /**
     * 存储解析和校验过程中的错误信息
     */
    private List<String> errorMessages=new ArrayList<>();
    /**
     * 处理每一行解析的数据
     * <p>
     * 当Excel的一行数据被解析完成后，该方法会被调用
     * 对解析的数据进行校验，如果校验通过则添加到数据列表中，否则记录错误信息
     * </p>
     * @param data 解析后的用户数据对象
     * @param context EasyExcel解析上下文，包含当前行信息等
     */
    @Override
    public void invoke(UserImportDTO data, AnalysisContext context){
        //数据校验
        if (isValid(data)) {
            dataList.add(data);
        }else{
            int rowIndex=context.readRowHolder().getRowIndex()+1;
            errorMessages.add("第"+rowIndex+"行数据校验失败");
        }
    }
    
    /**
     * 所有数据解析完成后的处理
     * <p>
     * 当Excel文件的所有数据都解析完成后，该方法会被调用
     * 可用于执行解析完成后的后续操作，如数据持久化等
     * </p>
     * @param context EasyExcel解析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context){
        // Excel解析完成，不输出日志
    }
    
    /**
     * 验证用户数据的有效性
     * <p>
     * 检查用户名和电话号码是否为空
     * </p>
     * @param data 用户数据对象
     * @return 数据是否有效
     */
    private boolean isValid(UserImportDTO data) {
        // 简单的数据校验逻辑
        return StringUtils.hasText(data.getUsername())
                && StringUtils.hasText(data.getPhone());
    }

    /**
     * 获取解析成功的用户数据列表
     * @return 解析成功的数据列表
     */
    public List<UserImportDTO> getDataList() {
        return dataList;
    }

    /**
     * 获取解析过程中的错误信息列表
     * @return 错误信息列表
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
