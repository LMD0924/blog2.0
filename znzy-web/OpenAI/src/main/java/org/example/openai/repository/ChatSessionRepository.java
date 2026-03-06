package org.example.openai.repository;

import org.example.openai.model.ChatSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话数据访问层（Repository）
 * 负责chat_sessions表的CRUD操作和数据库初始化
 * 管理聊天会话的元数据（如标题、创建时间、更新时间等）
 */
@Repository  // Spring注解，标记为数据访问组件
public class ChatSessionRepository {

    /**
     * 数据库表创建DDL语句
     * 创建会话表，存储聊天会话的基本信息
     */
    private static final String TABLE_DDL = """
            CREATE TABLE IF NOT EXISTS chat_sessions (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,                    -- 主键，自增长
                session_id VARCHAR(64) NOT NULL UNIQUE,                  -- 会话唯一标识，不允许重复
                title VARCHAR(255),                                      -- 会话标题，可为空
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认为当前时间
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间，默认为当前时间
                INDEX idx_sessions_updated (updated_at DESC)             -- 索引，按更新时间倒序排列，优化查询性能
            )
            """;

    // Spring JDBC模板，简化数据库操作
    private final JdbcTemplate jdbcTemplate;

    /**
     * 行映射器（RowMapper）
     * 将数据库ResultSet中的一行数据映射为ChatSession对象
     */
    private final RowMapper<ChatSession> rowMapper = new RowMapper<>() {
        @Override
        public ChatSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            // 创建ChatSession对象并设置属性
            ChatSession session = new ChatSession();
            session.setId(rs.getLong("id"));                          // 设置主键ID
            session.setSessionId(rs.getString("session_id"));         // 设置会话ID
            session.setTitle(rs.getString("title"));                  // 设置会话标题（可能为null）

            // 处理创建时间戳：如果数据库中的时间为null，使用当前时间
            Timestamp created = rs.getTimestamp("created_at");
            session.setCreatedAt(created != null ? created.toLocalDateTime() : LocalDateTime.now());

            // 处理更新时间戳：如果数据库中的时间为null，使用创建时间
            Timestamp updated = rs.getTimestamp("updated_at");
            session.setUpdatedAt(updated != null ? updated.toLocalDateTime() : session.getCreatedAt());

            return session;
        }
    };

    /**
     * 构造函数
     * 在创建Repository实例时初始化数据库表
     *
     * @param jdbcTemplate 通过Spring依赖注入的JdbcTemplate实例
     */
    public ChatSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 执行DDL语句，确保表存在
        this.jdbcTemplate.execute(TABLE_DDL);
    }

    /**
     * 保存会话到数据库
     * 如果是新会话，会自动设置创建时间和更新时间
     *
     * @param session 要保存的ChatSession对象
     * @return 保存后的ChatSession对象（包含生成的ID）
     */
    public ChatSession save(ChatSession session) {
        LocalDateTime now = LocalDateTime.now();

        // 如果会话没有创建时间，设置为当前时间
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(now);
        }

        // 如果会话没有更新时间，使用创建时间（或当前时间）
        if (session.getUpdatedAt() == null) {
            session.setUpdatedAt(session.getCreatedAt());
        }

        // 执行INSERT语句
        jdbcTemplate.update(
                "INSERT INTO chat_sessions(session_id, title, created_at, updated_at) VALUES (?, ?, ?, ?)",
                session.getSessionId(),                              // 会话ID
                session.getTitle(),                                  // 会话标题
                Timestamp.valueOf(session.getCreatedAt()),          // 创建时间
                Timestamp.valueOf(session.getUpdatedAt())           // 更新时间
        );

        // 获取自增ID并设置到对象中
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        session.setId(id);

        return session;
    }

    /**
     * 根据会话ID查找会话
     * 用于获取特定会话的详细信息
     *
     * @param sessionId 要查找的会话ID
     * @return ChatSession对象，如果不存在则返回null
     */
    public ChatSession findBySessionId(String sessionId) {
        List<ChatSession> sessions = jdbcTemplate.query(
                "SELECT * FROM chat_sessions WHERE session_id = ?",  // 按会话ID查询
                rowMapper,                                           // 使用行映射器
                sessionId                                            // 查询参数
        );

        // 如果查询结果为空，返回null；否则返回第一个结果
        return sessions.isEmpty() ? null : sessions.get(0);
    }

    /**
     * 查找所有会话
     * 按更新时间倒序排列，最新的会话在前
     *
     * @return 所有会话列表，按更新时间从新到旧排序
     */
    public List<ChatSession> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM chat_sessions ORDER BY updated_at DESC",  // 按更新时间倒序
                rowMapper
        );
    }

    /**
     * 更新空的会话标题
     * 只在当前标题为空或为空白字符串时才更新
     * 用于自动生成会话标题的场景
     *
     * @param sessionId 要更新的会话ID
     * @param title 新的标题
     */
    public void updateTitleIfEmpty(String sessionId, String title) {
        jdbcTemplate.update(
                // 只有当标题为NULL或空字符串时才更新
                "UPDATE chat_sessions SET title = ? WHERE session_id = ? AND (title IS NULL OR title = '')",
                title,       // 新标题
                sessionId    // 会话ID
        );
    }

    /**
     * 更新会话的更新时间（"触摸"会话）
     * 当会话有新的活动（如收到新消息）时调用，用于保持会话列表排序
     *
     * @param sessionId 要更新的会话ID
     * @param updatedAt 新的更新时间（通常为当前时间）
     */
    public void touchSession(String sessionId, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                "UPDATE chat_sessions SET updated_at = ? WHERE session_id = ?",
                Timestamp.valueOf(updatedAt),  // 新的更新时间
                sessionId                      // 会话ID
        );
    }

    /**
     * 根据会话ID删除会话
     * 删除前应先删除关联的聊天消息（外键约束或应用层逻辑）
     *
     * @param sessionId 要删除的会话ID
     */
    public void deleteBySessionId(String sessionId) {
        jdbcTemplate.update("DELETE FROM chat_sessions WHERE session_id = ?", sessionId);
    }
}
