package com.example.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传工具类
 *
 * 功能描述：
 * 1. 提供文件上传服务，将客户端上传的文件保存到服务器指定目录
 * 2. 生成唯一文件名，避免文件名冲突
 * 3. 验证文件类型，确保上传文件的安全性
 * 4. 返回文件访问URL，便于前端展示和访问
 *
 * 使用说明：
 * 1. 需要在application.properties中配置file.upload-dir属性指定上传目录
 * 2. 需要配置静态资源映射才能通过URL访问上传的文件
 * 3. 建议在Controller层调用isImageFile方法进行文件类型验证
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
@Component
public class FileUploadUtil {

    /**
     * 文件上传目录配置
     *
     * 从application.yml中读取file.upload-dir配置项
     * 示例配置：file.upload-dir=./uploads 或 file.upload-dir=/var/www/uploads
     *
     * 注意事项：
     * - 路径可以是相对路径或绝对路径
     * - 确保应用程序对该目录有读写权限
     * - 建议在生产环境中使用绝对路径
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 服务器端口号配置
     *
     * 从application.properties中读取server.port配置项
     * 用于构建完整的文件访问URL
     *
     * 默认值：如果未配置则使用Spring Boot默认端口8080
     */
    @Value("${server.port}")
    private String serverPort;

    /**
     * 上传文件到服务器指定目录
     *
     * 方法执行流程：
     * 1. 检查并创建上传目录（如果不存在）
     * 2. 生成基于UUID的唯一文件名，保留原始文件扩展名
     * 3. 将文件流保存到服务器磁盘
     * 4. 生成并返回文件的HTTP访问URL
     *
     * @param file 要上传的文件对象，Spring MVC提供的MultipartFile接口实例
     * @return String 上传成功后文件的完整HTTP访问URL，格式：http://localhost:port/images/filename
     * @throws IOException 当出现以下情况时抛出：
     *                    - 目录创建失败
     *                    - 文件保存过程中发生I/O错误
     *                    - 磁盘空间不足
     *                    - 权限不足无法写入文件
     *
     * @example
     * 输入：用户上传的图片文件 "avatar.jpg"
     * 输出： "http://localhost:8080/images/3b9f842c-7c3a-4b98-b6a2-8b7d95e3c7a1.jpg"
     *
     * @note 重要提醒：
     * - 该方法不会验证文件类型，调用前应使用isImageFile方法进行验证
     * - 返回的URL基于localhost，生产环境需要修改为实际域名
     * - 需要配置静态资源映射才能使返回的URL可访问
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 将上传目录字符串转换为Path对象，便于进行文件系统操作
        Path uploadPath = Paths.get(uploadDir);

        // 检查上传目录是否存在，如果不存在则递归创建所有必需的父目录
        // 这可以避免因目录不存在导致的FileNotFoundException
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 获取原始文件名，用于提取文件扩展名
        String originalFilename = file.getOriginalFilename();

        // 安全地提取文件扩展名：从最后一个点号开始截取到字符串末尾
        // 这样可以正确处理包含多个点号的文件名，如 "my.image.jpg"
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成基于UUID的唯一文件名，确保即使多个用户同时上传同名文件也不会冲突
        // UUID.randomUUID() 生成类似 "3b9f842c-7c3a-4b98-b6a2-8b7d95e3c7a1" 的唯一标识符
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 构建完整的文件保存路径：上传目录 + 唯一文件名
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 将MultipartFile的文件流复制到目标路径
        // 这是实际的文件保存操作，会将文件内容写入服务器磁盘
        Files.copy(file.getInputStream(), filePath);

        // 构建并返回文件的HTTP访问URL
        // 格式：http://localhost:端口号/images/文件名
        // 注意：需要配置静态资源映射才能使此URL生效
        return "http://localhost:" + serverPort + "/images/" + uniqueFileName;
    }

    /**
     * 验证上传文件是否为图片类型
     *
     * 该方法通过检查文件的MIME类型来验证是否为图片
     * 支持的图片类型包括：image/jpeg, image/png, image/gif, image/bmp等
     *
     * @param file 要验证的文件对象
     * @return boolean
     *         - true: 文件是图片类型（MIME类型以"image/"开头）
     *         - false: 文件不是图片类型或无法确定文件类型
     *
     * @example
     * isImageFile("avatar.jpg") → true (Content-Type: image/jpeg)
     * isImageFile("document.pdf") → false (Content-Type: application/pdf)
     * isImageFile("script.js") → false (Content-Type: application/javascript)
     *
     * @note 安全提醒：
     * - 仅依赖Content-Type验证并不完全安全，恶意用户可以伪造Content-Type
     * - 生产环境中建议结合文件魔数（magic number）进行更严格的验证
     * - 可以考虑使用Apache Tika等专业库进行文件类型检测
     */
    public boolean isImageFile(MultipartFile file) {
        // 获取文件的Content-Type，即MIME类型
        String contentType = file.getContentType();

        // 检查ContentType不为空且以"image/"开头
        // 所有标准图片类型的MIME类型都以"image/"为前缀
        return contentType != null && contentType.startsWith("image/");
    }
}