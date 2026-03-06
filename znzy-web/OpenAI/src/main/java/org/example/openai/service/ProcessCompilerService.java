package org.example.openai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.example.openai.dto.CompileRequest;
import org.example.openai.dto.CompileResponse;
import org.example.openai.util.FileUtil;
import org.example.openai.util.excel.ProcessExecutor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCompilerService implements CompilerService {

    private final FileUtil fileUtil;
    private final ProcessExecutor processExecutor;

    @Value("${compiler.workspace}")
    private String workspace;

    @Value("${compiler.timeout.compile}")
    private long compileTimeout;

    @Value("${compiler.timeout.execute}")
    private long executeTimeout;

    @Value("${compiler.security.max-code-length}")
    private int maxCodeLength;

    // 语言配置映射
    private static final Map<String, LanguageConfig> LANGUAGE_CONFIGS = new HashMap<>();

    static {
        // C语言配置
        LANGUAGE_CONFIGS.put("c", LanguageConfig.builder()
                .filename("main.c")
                .compileCommand(new String[]{"gcc", "-o", "main.exe", "main.c", "-lm"})
                .executeCommand(new String[]{".\\main.exe"})
                .needsCompilation(true)
                .build());

        // C++配置
        LANGUAGE_CONFIGS.put("cpp", LanguageConfig.builder()
                .filename("main.cpp")
                .compileCommand(new String[]{"g++", "-o", "main.exe", "main.cpp", "-std=c++11"})
                .executeCommand(new String[]{".\\main.exe"})
                .needsCompilation(true)
                .build());

        // Java配置
        LANGUAGE_CONFIGS.put("java", LanguageConfig.builder()
                .filename("Main.java")
                .compileCommand(new String[]{"javac", "Main.java"})
                .executeCommand(new String[]{"java", "Main"})
                .needsCompilation(true)
                .build());

        // Python配置
        LANGUAGE_CONFIGS.put("python", LanguageConfig.builder()
                .filename("main.py")
                .compileCommand(null)
                .executeCommand(new String[]{"python", "main.py"})
                .needsCompilation(false)
                .build());

        // JavaScript配置（需要安装Node.js）
        LANGUAGE_CONFIGS.put("javascript", LanguageConfig.builder()
                .filename("main.js")
                .compileCommand(null)
                .executeCommand(new String[]{"node", "main.js"})
                .needsCompilation(false)
                .build());
    }

    @Override
    public CompileResponse compileAndExecute(CompileRequest request) {
        // 1. 安全检查
        if (!checkCodeSafety(request.getLanguage(), request.getCode())) {
            return CompileResponse.error("代码安全检查不通过", "包含禁止使用的代码");
        }

        // 2. 生成会话ID
        String sessionId = "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        Path sessionPath = null;

        try {
            // 3. 创建工作目录
            sessionPath = fileUtil.createTempDirectory(workspace, sessionId);

            // 4. 获取语言配置
            LanguageConfig config = LANGUAGE_CONFIGS.get(request.getLanguage().toLowerCase());
            if (config == null) {
                return CompileResponse.error("不支持的语言: " + request.getLanguage());
            }

            // 5. 保存代码文件
            Path codeFile = fileUtil.saveCodeToFile(sessionPath, config.getFilename(), request.getCode());

            // 6. 保存输入文件（如果有）
            Path inputFile = null;
            if (request.getInput() != null && !request.getInput().trim().isEmpty()) {
                inputFile = fileUtil.saveInputToFile(sessionPath, request.getInput());
            }

            // 7. 编译（如果需要）
            String compileError = null;
            Long compileTime = null;

            if (config.isNeedsCompilation()) {
                log.info("开始编译 {} 代码", request.getLanguage());
                long startTime = System.currentTimeMillis();

                var compileResult = processExecutor.executeCommand(
                        config.getCompileCommand(),
                        sessionPath,
                        null,
                        compileTimeout
                );

                compileTime = System.currentTimeMillis() - startTime;

                if (compileResult.getExitCode() != 0) {
                    compileError = "编译失败:\n" + compileResult.getError();
                }

                log.info("编译完成，耗时: {}ms, 结果: {}", compileTime,
                        compileError == null ? "成功" : "失败");
            }

            // 8. 如果有编译错误，直接返回
            if (compileError != null) {
                return CompileResponse.error(compileError);
            }

            // 9. 执行程序
            log.info("开始执行 {} 程序", request.getLanguage());
            long startTime = System.currentTimeMillis();

            // 添加调试信息：检查工作目录中的文件
            try {
                log.info("工作目录: {}", sessionPath);
                Files.list(sessionPath).forEach(path -> {
                    log.info("目录中的文件: {}", path.getFileName());
                });
            } catch (IOException e) {
                log.error("检查工作目录文件失败", e);
            }

            // 检查可执行文件是否存在
            Path exePath = sessionPath.resolve("main.exe");
            if (!Files.exists(exePath)) {
                log.error("可执行文件不存在: {}", exePath);
                // 尝试列出所有可能的可执行文件
                try {
                    Files.list(sessionPath)
                        .filter(path -> path.toString().endsWith(".exe"))
                        .forEach(path -> log.info("找到的EXE文件: {}", path.getFileName()));
                } catch (IOException e) {
                    log.error("查找EXE文件失败", e);
                }
            }

            // 准备输入数据
            String inputData = inputFile != null ?
                    Files.readString(inputFile) : request.getInput();

            // 使用绝对路径执行可执行文件，并通过cmd.exe来运行（Windows兼容方案）
            Path absoluteExePath = sessionPath.resolve("main.exe");
            String[] absoluteCommand = {"cmd.exe", "/c", absoluteExePath.toString()};
            
            var executeResult = processExecutor.executeCommand(
                    absoluteCommand,
                    sessionPath,
                    inputData,
                    executeTimeout
            );

            long executeTime = System.currentTimeMillis() - startTime;
            log.info("执行完成，耗时: {}ms, 退出码: {}", executeTime, executeResult.getExitCode());

            // 10. 构建响应
            if (executeResult.isTimedOut()) {
                return CompileResponse.error("执行超时", "程序执行超过" + executeTimeout + "ms");
            }

            if (executeResult.getExitCode() == 0) {
                return CompileResponse.success(
                        executeResult.getOutput(),
                        executeTime
                );
            } else {
                return CompileResponse.error(
                        "程序执行错误 (退出码: " + executeResult.getExitCode() + "):\n" +
                                executeResult.getError()
                );
            }

        } catch (IOException e) {
            log.error("IO异常: ", e);
            return CompileResponse.error("系统IO异常", e.getMessage());
        } catch (Exception e) {
            log.error("未知异常: ", e);
            return CompileResponse.error("系统异常", e.getMessage());
        } finally {
            // 11. 清理临时文件
            if (sessionPath != null) {
                fileUtil.cleanupDirectory(sessionPath);
            }
        }
    }

    @Override
    public boolean checkCodeSafety(String language, String code) {
        // 1. 检查代码长度
        if (code.length() > maxCodeLength) {
            log.warn("代码长度超过限制: {} > {}", code.length(), maxCodeLength);
            return false;
        }

        // 2. 检查危险代码
        String lowerCode = code.toLowerCase();

        // 通用危险代码检查
        List<String> dangerousPatterns = Arrays.asList(
                "runtime\\.getruntime",
                "processbuilder",
                "system\\.exit",
                "exec\\(",
                "fork\\(",
                "popen\\(",
                "system\\("
        );

        for (String pattern : dangerousPatterns) {
            if (lowerCode.contains(pattern)) {
                log.warn("检测到危险代码: {}", pattern);
                return false;
            }
        }

        // 3. 语言特定的安全检查
        switch (language.toLowerCase()) {
            case "java":
                return !lowerCode.contains("reflect") &&
                        !lowerCode.contains("unsafe");
            case "c":
            case "cpp":
                return !lowerCode.contains("asm") &&
                        !lowerCode.contains("syscall");
            case "python":
                return !lowerCode.contains("__import__") &&
                        !lowerCode.contains("eval(");
            default:
                return true;
        }
    }

    @Override
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(LANGUAGE_CONFIGS.keySet());
    }

    /**
     * 语言配置类
     */
    @lombok.Builder
    @lombok.Data
    private static class LanguageConfig {
        private String filename;
        private String[] compileCommand;
        private String[] executeCommand;
        private boolean needsCompilation;
    }
}