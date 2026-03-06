package org.example.openai.util;

/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description:
 */
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class FileUtil {

    /**
     * 创建临时工作目录
     */
    public Path createTempDirectory(String basePath, String sessionId) throws IOException {
        Path sessionPath = Paths.get(basePath, sessionId);
        Files.createDirectories(sessionPath);
        log.info("创建临时目录: {}", sessionPath);
        return sessionPath;
    }

    /**
     * 保存代码到文件
     */
    public Path saveCodeToFile(Path directory, String filename, String content) throws IOException {
        Path filePath = directory.resolve(filename);
        Files.writeString(filePath, content);
        log.info("保存代码文件: {}", filePath);
        return filePath;
    }

    /**
     * 保存输入到文件
     */
    public Path saveInputToFile(Path directory, String input) throws IOException {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        Path inputPath = directory.resolve("input.txt");
        Files.writeString(inputPath, input);
        log.info("保存输入文件: {}", inputPath);
        return inputPath;
    }

    /**
     * 清理临时目录
     */
    public void cleanupDirectory(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try {
            Files.walk(directory)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("删除文件: {}", path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {}", path);
                        }
                    });
            log.info("清理目录: {}", directory);
        } catch (IOException e) {
            log.error("清理目录失败: {}", directory, e);
        }
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "python" -> "py";
            case "c" -> "c";
            case "cpp" -> "cpp";
            case "javascript" -> "js";
            default -> "txt";
        };
    }
}
