package org.example.openai.repository;

import org.example.openai.model.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息数据访问层（Repository）
 * 负责chat_messages表的CRUD操作和数据库初始化
 */
@Repository  // Spring注解，标记为数据访问组件，会被Spring自动扫描和管理
public class ChatMessageRepository {

    /**
     * 数据库表创建DDL语句
     * 使用多行字符串（Java 15+特性）定义表结构
     * 如果表不存在则创建，包含索引优化
     */
    private static final String TABLE_DDL = """
            CREATE TABLE IF NOT EXISTS chat_messages (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,          -- 主键，自增长
                session_id VARCHAR(64) NOT NULL,               -- 会话ID，关联chat_sessions表
                role VARCHAR(16) NOT NULL,                     -- 消息角色：user/assistant/system
                content TEXT NOT NULL,                         -- 消息内容，使用TEXT类型存储长文本
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间，默认为当前时间
                INDEX idx_session_time(session_id, created_at) -- 复合索引，优化按会话和时间的查询
            )
            """;

    // JdbcTemplate是Spring对JDBC的封装，简化数据库操作
    private final JdbcTemplate jdbcTemplate;

    /**
     * 行映射器（RowMapper）
     * 用于将ResultSet中的一行数据映射为ChatMessage对象
     */
    private final RowMapper<ChatMessage> rowMapper = new RowMapper<>() {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            // 创建ChatMessage对象并设置属性
            ChatMessage message = new ChatMessage();
            message.setId(rs.getLong("id"));                          // 设置ID
            message.setSessionId(rs.getString("session_id"));         // 设置会话ID
            message.setRole(rs.getString("role"));                    // 设置角色
            message.setContent(rs.getString("content"));              // 设置内容

            // 处理时间戳：如果数据库中的时间为null，使用当前时间
            Timestamp timestamp = rs.getTimestamp("created_at");
            message.setCreatedAt(timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now());

            return message;
        }
    };

    /**
     * 构造函数
     * 在创建Repository实例时初始化数据库表
     *
     * @param jdbcTemplate 通过Spring依赖注入的JdbcTemplate实例
     */
    public ChatMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 执行DDL语句，确保表存在
        this.jdbcTemplate.execute(TABLE_DDL);
    }

    /**
     * 保存消息到数据库
     *
     * @param message 要保存的ChatMessage对象
     * @return 保存后的ChatMessage对象（包含生成的ID）
     */
    public ChatMessage save(ChatMessage message) {
        // 如果消息没有创建时间，设置为当前时间
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }

        // 执行INSERT语句
        jdbcTemplate.update(
                "INSERT INTO chat_messages(session_id, role, content, created_at) VALUES (?, ?, ?, ?)",
                message.getSessionId(),                              // 会话ID
                message.getRole(),                                   // 角色
                message.getContent(),                                // 内容
                Timestamp.valueOf(message.getCreatedAt())           // 创建时间（转换为SQL Timestamp）
        );

        // 获取自增ID并设置到对象中
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        message.setId(id);

        return message;
    }

    /**
     * 查找指定会话的最近历史消息
     * 用于构建AI对话上下文，限制返回数量避免token超限
     *
     * @param sessionId 会话ID
     * @param limit 返回的最大消息数量
     * @return 按时间正序排列的消息列表
     */
    public List<ChatMessage> findRecentHistory(String sessionId, int limit) {
        // 查询最新的limit条消息（按时间倒序）
        return jdbcTemplate.query(
                        "SELECT * FROM chat_messages WHERE session_id = ? ORDER BY created_at DESC LIMIT ?",
                        rowMapper,     // 使用行映射器转换结果
                        sessionId,     // 参数1：会话ID
                        limit          // 参数2：限制数量
                ).stream()
                // 将倒序结果转为正序（最早的在前，最新的在后）
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .toList();
    }

    /**
     * 查找指定会话的全部历史消息
     * 用于展示完整的对话历史
     *
     * @param sessionId 会话ID
     * @return 按时间正序排列的全部消息
     */
    public List<ChatMessage> findFullHistory(String sessionId) {
        return jdbcTemplate.query(
                "SELECT * FROM chat_messages WHERE session_id = ? ORDER BY created_at ASC",
                rowMapper,
                sessionId
        );
    }

    /**
     * 删除指定会话的所有消息
     * 当用户清空聊天记录或删除会话时调用
     *
     * @param sessionId 要删除消息的会话ID
     */
    public void deleteBySessionId(String sessionId) {
        jdbcTemplate.update("DELETE FROM chat_messages WHERE session_id = ?", sessionId);
    }

    /**
     * 修剪历史消息，保持消息数量不超过限制
     * 用于控制数据库存储大小和AI上下文长度
     *
     * @param sessionId 会话ID
     * @param maxMessages 允许保留的最大消息数量
     */
    public void trimHistory(String sessionId, int maxMessages) {
        // 1. 首先统计当前会话的消息总数
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chat_messages WHERE session_id = ?",
                Integer.class,
                sessionId
        );

        // 2. 如果消息数未超过限制，直接返回
        if (count == null || count <= maxMessages) {
            return;
        }

        // 3. 计算需要删除的消息数量
        int toRemove = count - maxMessages;

        // 4. 删除最早的消息（按创建时间排序）
        jdbcTemplate.update(
                """
                DELETE FROM chat_messages
                WHERE id IN (
                    SELECT id FROM (
                        SELECT id FROM chat_messages
                        WHERE session_id = ?
                        ORDER BY created_at ASC     -- 按时间正序，最早的在前
                        LIMIT ?                     -- 限制删除数量
                    ) t
                )
                """,
                sessionId,
                toRemove
        );
    }
}