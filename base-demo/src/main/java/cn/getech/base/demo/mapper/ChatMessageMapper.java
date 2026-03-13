package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
@CacheNamespace(flushInterval = 60000, size = 512)
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    @ResultMap("chatMessageMap")
    List<ChatMessage> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND sync_status = 0 AND is_deleted = 0 ORDER BY id ASC")
    @ResultMap("chatMessageMap")
    List<ChatMessage> selectUnsyncedMessages(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND is_deleted = 0 ORDER BY id ASC")
    @ResultMap("chatMessageMap")
    List<ChatMessage> selectAllValidMessages(@Param("sessionId") String sessionId);

    @Select("SELECT id FROM chat_message WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY id ASC LIMIT #{limit}")
    List<Long> selectOldMessageIds(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM chat_message WHERE workflow_execution_id = #{executionId} AND is_deleted = 0 ORDER BY create_time")
    @ResultMap("chatMessageMap")
    List<ChatMessage> selectByExecutionId(@Param("executionId") String executionId);

    @Select("SELECT * FROM chat_message WHERE sync_status = #{syncStatus} AND is_deleted = 0 ORDER BY create_time ASC LIMIT #{limit}")
    @ResultMap("chatMessageMap")
    List<ChatMessage> selectBySyncStatus(@Param("syncStatus") Integer syncStatus, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM chat_message WHERE user_id = #{userId} AND is_deleted = 0")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND message_type = #{messageType} AND is_deleted = 0")
    int countBySessionIdAndType(@Param("sessionId") String sessionId, @Param("messageType") Integer messageType);

    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND sync_status = #{syncStatus} AND is_deleted = 0")
    int countBySessionIdAndSyncStatus(@Param("sessionId") String sessionId, @Param("syncStatus") Integer syncStatus);

    @Select("SELECT COUNT(*) FROM chat_message WHERE is_deleted = 0")
    int countAllValidMessages();

    @Select("SELECT COUNT(*) FROM chat_message WHERE sync_status = 0 AND is_deleted = 0")
    int countUnsyncedMessages();

    @Select("SELECT COUNT(*) FROM chat_message WHERE sync_status = 2 AND is_deleted = 0")
    int countSyncedMessages();


    @Select("""
            SELECT 
                session_id as sessionId,
                COUNT(*) as messageCount,
                SUM(CASE WHEN is_ai_response = 1 THEN 1 ELSE 0 END) as aiMessageCount,
                SUM(CASE WHEN is_ai_response = 0 THEN 1 ELSE 0 END) as userMessageCount
            FROM chat_message
            WHERE is_deleted = 0
            GROUP BY session_id
            ORDER BY message_count DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getMessageStatsBySession(@Param("limit") int limit);


    @Update("UPDATE chat_message SET sync_status = #{syncStatus}, sync_time = NOW() WHERE id = #{messageId}")
    int updateSyncStatus(@Param("messageId") Long messageId, @Param("syncStatus") Integer syncStatus);

    @Update("UPDATE chat_message SET sync_status = 2, sync_time = NOW() WHERE session_id = #{sessionId} AND sync_status = 0 AND is_deleted = 0")
    int updateSyncStatusToSynced(@Param("sessionId") String sessionId);

    @Update("UPDATE chat_message SET sync_status = 3, sync_time = NOW() WHERE session_id = #{sessionId} AND sync_status = 0 AND is_deleted = 0")
    int updateSyncStatusToFailed(@Param("sessionId") String sessionId);

    @Update("UPDATE chat_message SET is_deleted = 1 WHERE id = #{messageId}")
    int markAsDeleted(@Param("messageId") Long messageId);


    @Update("""
            <script>
                UPDATE chat_message SET is_deleted = 1 WHERE id IN 
                <foreach collection="messageIds" item="id" open="(" separator="," close=")">
                    #{id}
                </foreach>
            </script>
    """)
    int batchMarkAsDeleted(@Param("messageIds") List<Long> messageIds);


    @Update("UPDATE chat_message SET metadata = #{metadata} WHERE id = #{messageId}")
    int updateMetadata(@Param("messageId") Long messageId, @Param("metadata") String metadata);

    @Delete("DELETE FROM chat_message WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);

    @Delete("DELETE FROM chat_message WHERE workflow_execution_id = #{executionId}")
    int deleteByExecutionId(@Param("executionId") String executionId);

    @Delete("DELETE FROM chat_message WHERE create_time < #{expireTime}")
    int deleteExpired(@Param("expireTime") LocalDateTime expireTime);


    @Insert("""
            INSERT INTO chat_message 
            (session_id, message_type, content, intent, sentiment, is_ai_response, 
             workflow_execution_id, sync_status, response_time, metadata) 
            VALUES 
            <foreach collection="list" item="item" separator=",">
                (#{item.sessionId}, #{item.messageType}, #{item.content}, #{item.intent}, 
                 #{item.sentiment}, #{item.isAiResponse}, #{item.workflowExecutionId}, 
                 #{item.syncStatus}, #{item.responseTime}, #{item.metadata})
            </foreach>
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("list") List<ChatMessage> messages);


    @Select("""
            <script>
                SELECT * FROM chat_message 
                WHERE is_deleted = 0
                <if test="sessionId != null and sessionId != ''">
                    AND session_id = #{sessionId}
                </if>
                <if test="messageType != null">
                    AND message_type = #{messageType}
                </if>
                <if test="isAiResponse != null">
                    AND is_ai_response = #{isAiResponse}
                </if>
                <if test="syncStatus != null">
                    AND sync_status = #{syncStatus}
                </if>
                <if test="startTime != null">
                    AND create_time >= #{startTime}
                </if>
                <if test="endTime != null">
                    AND create_time &lt;= #{endTime}
                </if>
                <if test="keyword != null and keyword != ''">
                    AND content LIKE CONCAT('%', #{keyword}, '%')
                </if>
                ORDER BY create_time DESC
                LIMIT #{limit}
            </script>
    """)
    @ResultMap("chatMessageMap")
    List<ChatMessage> searchMessages(@Param("sessionId") String sessionId,
                                     @Param("messageType") Integer messageType,
                                     @Param("isAiResponse") Integer isAiResponse,
                                     @Param("syncStatus") Integer syncStatus,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("keyword") String keyword,
                                     @Param("limit") int limit);


    @Select("""
            SELECT 
                DATE(create_time) as date,
                COUNT(*) as totalMessages,
                SUM(CASE WHEN is_ai_response = 1 THEN 1 ELSE 0 END) as aiMessages,
                SUM(CASE WHEN is_ai_response = 0 THEN 1 ELSE 0 END) as userMessages,
                AVG(response_time) as avgResponseTime
            FROM chat_message
            WHERE is_deleted = 0 AND create_time >= #{startTime}
            GROUP BY DATE(create_time)
            ORDER BY date DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getDailyMessageStats(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);


    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND is_ai_response = 1 AND is_deleted = 0 ORDER BY create_time")
    @ResultMap("chatMessageMap")
    List<ChatMessage> selectAiMessagesBySession(@Param("sessionId") String sessionId);


    @Select("""
            SELECT 
                AVG(response_time) as avgResponseTime,
                MIN(response_time) as minResponseTime,
                MAX(response_time) as maxResponseTime,
                COUNT(*) as totalAiMessages
            FROM chat_message
            WHERE is_ai_response = 1 AND response_time IS NOT NULL AND is_deleted = 0
            AND create_time >= #{startTime}
    """)
    Map<String, Object> getAiResponseStats(@Param("startTime") LocalDateTime startTime);


    @Results(id = "chatMessageMap", value = {
            @Result(property = "id", column = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(property = "sessionId", column = "session_id", jdbcType = JdbcType.VARCHAR),
            @Result(property = "messageType", column = "message_type", jdbcType = JdbcType.TINYINT),
            @Result(property = "content", column = "content", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "intent", column = "intent", jdbcType = JdbcType.VARCHAR),
            @Result(property = "sentiment", column = "sentiment", jdbcType = JdbcType.VARCHAR),
            @Result(property = "isAiResponse", column = "is_ai_response", jdbcType = JdbcType.TINYINT),
            @Result(property = "workflowExecutionId", column = "workflow_execution_id", jdbcType = JdbcType.VARCHAR),
            @Result(property = "syncStatus", column = "sync_status", jdbcType = JdbcType.TINYINT),
            @Result(property = "syncTime", column = "sync_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "responseTime", column = "response_time", jdbcType = JdbcType.INTEGER),
            @Result(property = "isDeleted", column = "is_deleted", jdbcType = JdbcType.TINYINT),
            @Result(property = "metadata", column = "metadata", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "createTime", column = "create_time", jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("SELECT * FROM chat_message WHERE id = #{id}")
    ChatMessage selectByIdWithMapping(@Param("id") Long id);


    @Select("""
            SELECT 
                HOUR(create_time) as hour,
                COUNT(*) as messageCount
            FROM chat_message
            WHERE is_deleted = 0 AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            GROUP BY HOUR(create_time)
            ORDER BY hour
    """)
    List<Map<String, Object>> getHourlyMessageStats();


    @Select("""
            SELECT 
                intent,
                COUNT(*) as count,
                AVG(LENGTH(content)) as avgContentLength
            FROM chat_message
            WHERE intent IS NOT NULL AND intent != '' AND is_deleted = 0
            AND create_time >= #{startTime}
            GROUP BY intent
            ORDER BY count DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getIntentStats(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);


    @Select("""
            SELECT 
                sentiment,
                COUNT(*) as count
            FROM chat_message
            WHERE sentiment IS NOT NULL AND sentiment != '' AND is_deleted = 0
            AND create_time >= #{startTime}
            GROUP BY sentiment
            ORDER BY count DESC
    """)
    List<Map<String, Object>> getSentimentStats(@Param("startTime") LocalDateTime startTime);

}
