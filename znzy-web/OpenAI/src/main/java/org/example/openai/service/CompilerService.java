package org.example.openai.service;

/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description:
 */

import org.example.openai.dto.CompileRequest;
import org.example.openai.dto.CompileResponse;

import java.util.List;

public interface CompilerService {

    /**
     * 编译并执行代码
     */
    CompileResponse compileAndExecute(CompileRequest request);

    /**
     * 检查代码安全性
     */
    boolean checkCodeSafety(String language, String code);

    /**
     * 获取支持的语言列表
     */
    List<String> getSupportedLanguages();
}
