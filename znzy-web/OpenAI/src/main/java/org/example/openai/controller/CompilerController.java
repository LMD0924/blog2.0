package org.example.openai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.openai.dto.CompileRequest;
import org.example.openai.dto.CompileResponse;
import org.example.openai.model.RestBean;
import org.example.openai.service.CompilerService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description: 在线编译器控制器 - 修正版
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompilerController {

    private final CompilerService compilerService;

    /**
     * 编译执行代码 - ✅ 修复：添加明确的路径
     * 路径：POST /api/compile
     */
    @PostMapping("/compile")
    public RestBean<CompileResponse> compile(@Valid @RequestBody CompileRequest request,
                                             BindingResult bindingResult) {
        log.debug("收到编译请求: {}", request);

        // 参数验证错误处理
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            Map<String, String> errors = fieldErrors.stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing + ", " + replacement
                    ));

            log.warn("参数验证失败: {}", errors);
            return RestBean.failure("参数验证失败");
        }

        log.info("收到编译请求 - 语言: {}, 代码长度: {}, 输入长度: {}",
                request.getLanguage(),
                request.getCode().length(),
                request.getInput() != null ? request.getInput().length() : 0);

        try {
            CompileResponse response = compilerService.compileAndExecute(request);

            log.info("编译结果 - 状态: {}, 执行时间: {}ms, 输出长度: {}",
                    response.getStatus(),
                    response.getExecutionTime(),
                    response.getOutput() != null ? response.getOutput().length() : 0);

            return RestBean.success("编译执行完成", response);

        } catch (Exception e) {
            log.error("编译执行异常: ", e);
            return RestBean.failure("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取支持的语言列表
     * 路径：GET /api/languages
     */
    @GetMapping("/languages")
    public RestBean<Map<String, Object>> getLanguages() {
        try {
            List<String> languages = compilerService.getSupportedLanguages();

            Map<String, Object> result = new HashMap<>();
            result.put("languages", languages);
            result.put("count", languages.size());
            result.put("supported", languages.contains("java") &&
                    languages.contains("python") &&
                    languages.contains("c"));
            result.put("timestamp", System.currentTimeMillis());

            log.debug("返回语言列表: {}", languages);
            return RestBean.success("获取语言列表成功", result);

        } catch (Exception e) {
            log.error("获取语言列表异常: ", e);
            return RestBean.failure("获取语言列表失败");
        }
    }

    /**
     * 健康检查
     * 路径：GET /api/health
     */
    @GetMapping("/health")
    public RestBean<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Online Compiler");
        health.put("version", "1.0.0");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));

        // 检查关键服务
        try {
            List<String> languages = compilerService.getSupportedLanguages();
            health.put("languages", String.join(",", languages));
            health.put("available", "true");
            log.debug("健康检查通过，支持语言: {}", languages);
        } catch (Exception e) {
            health.put("available", "false");
            health.put("error", e.getMessage());
            log.warn("健康检查失败: {}", e.getMessage());
        }

        return RestBean.success("服务运行正常", health);
    }

    /**
     * 测试接口
     * 路径：GET /api/test
     */
    @GetMapping("/test")
    public RestBean<Map<String, String>> test() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "Online Compiler API is working!");
        result.put("version", "1.0.0");
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));
        result.put("endpoint", "/api/test");

        log.debug("测试接口被调用");
        return RestBean.success("测试接口正常", result);
    }

    /**
     * 获取编译器信息
     * 路径：GET /api/info
     */
    @GetMapping("/info")
    public RestBean<Map<String, Object>> getCompilerInfo() {
        Map<String, Object> info = new HashMap<>();

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        info.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + "MB");
        info.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + "MB");
        info.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + "MB");
        info.put("availableProcessors", runtime.availableProcessors());

        // 系统信息
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osArch", System.getProperty("os.arch"));

        // 服务信息
        List<String> languages = compilerService.getSupportedLanguages();
        info.put("supportedLanguages", languages);
        info.put("languageCount", languages.size());

        log.debug("返回编译器信息");
        return RestBean.success("获取编译器信息成功", info);
    }

    /**
     * 版本信息
     * 路径：GET /api/version
     */
    @GetMapping("/version")
    public RestBean<Map<String, String>> getVersion() {
        Map<String, String> version = new HashMap<>();
        version.put("name", "Online Compiler");
        version.put("version", "1.0.0");
        version.put("description", "在线代码编译执行服务");
        version.put("author", "总会落叶");
        version.put("repository", "https://github.com/yourname/online-compiler");
        version.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return RestBean.success(version);
    }

    /**
     * 示例代码
     * 路径：GET /api/examples/{language}
     */
    @GetMapping("/examples/{language}")
    public RestBean<Map<String, String>> getExampleCode(@PathVariable String language) {
        log.debug("请求示例代码，语言: {}", language);

        Map<String, String> examples = new HashMap<>();

        switch (language.toLowerCase()) {
            case "java":
                examples.put("hello",
                        "public class Main {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        System.out.println(\"Hello, World!\");\n" +
                                "    }\n" +
                                "}");
                examples.put("fibonacci",
                        "public class Main {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        int n = 10;\n" +
                                "        System.out.println(\"Fibonacci(\" + n + \"): \" + fibonacci(n));\n" +
                                "    }\n" +
                                "    \n" +
                                "    static int fibonacci(int n) {\n" +
                                "        if (n <= 1) return n;\n" +
                                "        return fibonacci(n-1) + fibonacci(n-2);\n" +
                                "    }\n" +
                                "}");
                break;

            case "python":
                examples.put("hello", "print(\"Hello, World!\")");
                examples.put("fibonacci",
                        "def fibonacci(n):\n" +
                                "    if n <= 1:\n" +
                                "        return n\n" +
                                "    return fibonacci(n-1) + fibonacci(n-2)\n" +
                                "\n" +
                                "n = 10\n" +
                                "print(f\"Fibonacci({n}): {fibonacci(n)}\")");
                break;

            case "c":
                examples.put("hello",
                        "#include <stdio.h>\n" +
                                "\n" +
                                "int main() {\n" +
                                "    printf(\"Hello, World!\\n\");\n" +
                                "    return 0;\n" +
                                "}");
                examples.put("fibonacci",
                        "#include <stdio.h>\n" +
                                "\n" +
                                "int fibonacci(int n) {\n" +
                                "    if (n <= 1) return n;\n" +
                                "    return fibonacci(n-1) + fibonacci(n-2);\n" +
                                "}\n" +
                                "\n" +
                                "int main() {\n" +
                                "    int n = 10;\n" +
                                "    printf(\"Fibonacci(%d): %d\\n\", n, fibonacci(n));\n" +
                                "    return 0;\n" +
                                "}");
                break;

            case "javascript":
                examples.put("hello", "console.log('Hello, World!');");
                examples.put("fibonacci",
                        "function fibonacci(n) {\n" +
                                "    if (n <= 1) return n;\n" +
                                "    return fibonacci(n-1) + fibonacci(n-2);\n" +
                                "}\n" +
                                "\n" +
                                "const n = 10;\n" +
                                "console.log(`Fibonacci(${n}): ${fibonacci(n)}`);");
                break;

            case "cpp":
                examples.put("hello",
                        "#include <iostream>\n" +
                                "using namespace std;\n" +
                                "\n" +
                                "int main() {\n" +
                                "    cout << \"Hello, World!\" << endl;\n" +
                                "    return 0;\n" +
                                "}");
                examples.put("fibonacci",
                        "#include <iostream>\n" +
                                "using namespace std;\n" +
                                "\n" +
                                "int fibonacci(int n) {\n" +
                                "    if (n <= 1) return n;\n" +
                                "    return fibonacci(n-1) + fibonacci(n-2);\n" +
                                "}\n" +
                                "\n" +
                                "int main() {\n" +
                                "    int n = 10;\n" +
                                "    cout << \"Fibonacci(\" << n << \"): \" << fibonacci(n) << endl;\n" +
                                "    return 0;\n" +
                                "}");
                break;

            default:
                log.warn("请求了不支持的语言示例: {}", language);
                return RestBean.failure("暂未提供该语言的示例代码");
        }

        return RestBean.success("获取示例代码成功", examples);
    }

    /**
     * 获取所有支持的示例语言
     * 路径：GET /api/examples
     */
    @GetMapping("/examples")
    public RestBean<Map<String, Object>> getAllExamples() {
        Map<String, Object> result = new HashMap<>();
        result.put("supportedLanguages", List.of("java", "python", "c", "javascript", "cpp"));
        result.put("description", "获取示例代码请访问 /api/examples/{language}");
        result.put("timestamp", System.currentTimeMillis());

        return RestBean.success("获取支持语言成功", result);
    }

    /**
     * 服务状态监控
     * 路径：GET /api/status
     */
    @GetMapping("/status")
    public RestBean<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();

        // 服务状态
        status.put("service", "online-compiler");
        status.put("status", "RUNNING");
        status.put("uptime", System.currentTimeMillis() - getStartTime());

        // 资源使用情况
        Runtime runtime = Runtime.getRuntime();
        status.put("memoryUsed", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + "MB");
        status.put("memoryTotal", runtime.totalMemory() / 1024 / 1024 + "MB");

        // 编译服务状态
        try {
            List<String> languages = compilerService.getSupportedLanguages();
            status.put("compilerStatus", "READY");
            status.put("availableLanguages", languages.size());
        } catch (Exception e) {
            status.put("compilerStatus", "ERROR");
            status.put("error", e.getMessage());
        }

        status.put("timestamp", System.currentTimeMillis());

        return RestBean.success("服务状态查询成功", status);
    }

    // 模拟启动时间
    private static final long START_TIME = System.currentTimeMillis();
    private long getStartTime() {
        return START_TIME;
    }
}
