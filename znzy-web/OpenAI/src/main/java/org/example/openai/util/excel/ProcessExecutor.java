package org.example.openai.util.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description: 进程执行工具类，用于安全地执行外部系统命令并获取执行结果
 */
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;
import org.apache.commons.exec.ExecuteException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.exec.PumpStreamHandler;

@Slf4j
@Component  // 声明为Spring组件，可以通过依赖注入使用
public class ProcessExecutor {

    /**
     * 执行外部系统命令并获取执行结果
     *
     * @param command 命令数组，第一个元素是命令本身，后续元素是参数
     *                例如: ["ls", "-l", "-a"]
     * @param workingDir 命令执行的工作目录路径，可以为null表示使用当前目录
     * @param input 要传递给命令的输入内容（当前方法未使用此参数）
     * @param timeoutMs 命令执行超时时间（毫秒），超时后进程将被终止
     * @return ProcessResult 包含命令执行结果的对象
     * @throws IOException 当执行过程中发生IO异常时抛出
     */
    public ProcessResult executeCommand(String[] command, Path workingDir, String input, long timeoutMs)
            throws IOException {

        // 创建字节数组输出流，用于捕获命令的标准输出和错误输出
        // 使用ByteArrayOutputStream可以避免传统IO可能导致的死锁问题
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();  // 标准输出流
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();   // 错误输出流

        // 构建命令行对象：第一个参数是命令本身，后续参数是命令的参数
        // CommandLine会自动处理参数中的空格和特殊字符，避免命令注入攻击
        CommandLine cmdLine = new CommandLine(command[0]);  // 设置主命令
        for (int i = 1; i < command.length; i++) {
            cmdLine.addArgument(command[i]);  // 逐个添加命令参数
        }

        // 创建执行器实例
        DefaultExecutor executor = new DefaultExecutor();

        // 设置命令执行的工作目录，如果为null则使用当前JVM的工作目录
        if (workingDir != null) {
            executor.setWorkingDirectory(workingDir.toFile());
        }

        // 设置流处理器：将进程的输出流重定向到我们创建的字节数组流中
        // PumpStreamHandler负责将进程的stdout和stderr流内容泵送到指定的输出流
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));

        // 创建看门狗，用于监控命令执行时间，超时则终止进程
        // 这是防止进程无限期运行的重要机制
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutMs);
        executor.setWatchdog(watchdog);  // 将看门狗关联到执行器

        try {
            // 记录命令执行日志，用于调试和监控
            log.info("执行命令: {}", String.join(" ", command));
            long startTime = System.currentTimeMillis();  // 记录开始时间，用于计算执行耗时

            // 执行命令并获取退出码（阻塞当前线程直到命令执行完成）
            // exitValue为0通常表示命令执行成功，非0表示执行失败
            int exitValue = executor.execute(cmdLine);
            long endTime = System.currentTimeMillis();  // 记录结束时间

            // 将字节流转换为字符串，获取命令的输出内容
            // 注意：这里使用平台默认字符编码，可能需要根据实际情况指定编码
            String output = outputStream.toString();  // 标准输出内容
            String error = errorStream.toString();    // 错误输出内容

            // 构建并返回成功执行的结果对象
            return ProcessResult.builder()
                    .exitCode(exitValue)                   // 命令退出码
                    .output(output)                        // 标准输出内容
                    .error(error)                          // 错误输出内容
                    .executionTime(endTime - startTime)    // 计算执行耗时（毫秒）
                    .timedOut(false)                       // 标记为未超时
                    .build();

        } catch (ExecuteException e) {
            // 捕获执行异常：命令执行失败，但进程已正常退出（非零退出码）
            // 获取命令执行过程中产生的输出内容
            String output = outputStream.toString();
            String error = errorStream.toString();

            // 构建并返回异常执行的结果对象
            return ProcessResult.builder()
                    .exitCode(e.getExitValue())            // 获取异常中的退出码
                    .output(output)                        // 标准输出内容（如果有）
                    .error(error)                          // 错误输出内容
                    .timedOut(watchdog.killedProcess())    // 判断是否因超时被看门狗终止
                    .build();
        }
        // 注意：这里没有捕获所有异常，因为IOException已经声明抛出
        // 这样可以允许调用者处理其他类型的异常
    }

    /**
     * 进程执行结果的数据传输对象
     * 使用Lombok注解自动生成构建器模式方法和getter/setter
     */
    @lombok.Builder   // 生成构建器模式：ProcessResult.builder().exitCode(0).build()
    @lombok.Data      // 生成getter、setter、toString、equals、hashCode方法
    public static class ProcessResult {
        /**
         * 进程退出码
         * 通常约定：0表示成功，非0表示失败
         */
        private int exitCode;

        /**
         * 标准输出内容
         * 命令执行过程中打印到stdout的内容
         */
        private String output;

        /**
         * 错误输出内容
         * 命令执行过程中打印到stderr的内容
         */
        private String error;

        /**
         * 命令执行耗时（毫秒）
         * 仅当命令正常执行完成时才有值，异常时可能为null
         */
        private Long executionTime;

        /**
         * 是否因超时被终止
         * true: 命令执行超时，被看门狗强制终止
         * false: 命令正常完成或异常退出，但未超时
         */
        private boolean timedOut;

        // Lombok注解会自动生成以下方法：
        // ProcessResult.builder() - 创建构建器
        // getExitCode(), setExitCode() - getter/setter
        // toString(), equals(), hashCode() - 常用Object方法
        // builder().exitCode(0).output("").build() - 链式构建
    }
}