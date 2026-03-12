package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.ChatSession;
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
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    @Select("SELECT * FROM chat_session WHERE session_id = #{sessionId}")
    @ResultMap("chatSessionMap")
    ChatSession selectBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} ORDER BY last_message_time DESC LIMIT #{limit}")
    @ResultMap("chatSessionMap")
    List<ChatSession> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM chat_session WHERE status = #{status} ORDER BY last_message_time DESC LIMIT #{limit}")
    @ResultMap("chatSessionMap")
    List<ChatSession> selectByStatus(@Param("status") Integer status, @Param("limit") int limit);

    @Select("SELECT * FROM chat_session WHERE session_type = #{sessionType} ORDER BY last_message_time DESC LIMIT #{limit}")
    @ResultMap("chatSessionMap")
    List<ChatSession> selectBySessionType(@Param("sessionType") Integer sessionType, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM chat_session WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM chat_session WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);


    @Select("""
            SELECT 
                DATE(create_time) as date,
                COUNT(*) as totalSessions,
                SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as activeSessions,
                AVG(message_count) as avgMessages,
                SUM(message_count) as totalMessages
            FROM chat_session
            WHERE create_time >= #{startTime}
            GROUP BY DATE(create_time)
            ORDER BY date DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getDailyStats(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);


    @Select("""
            SELECT 
                session_type AS sessionType,
                COUNT(*) as sessionCount,
                AVG(message_count) as avgMessages,
                AVG(TIMESTAMPDIFF(SECOND, start_time, COALESCE(end_time, NOW()))) as avgDurationSeconds
            FROM chat_session
            WHERE start_time >= #{startTime}
            GROUP BY session_type
            ORDER BY session_count DESC
    """)
    List<Map<String, Object>> getStatsBySessionType(@Param("startTime") LocalDateTime startTime);


    @Update("UPDATE chat_session SET message_count = message_count + 1, last_message_time = #{lastMessageTime} WHERE session_id = #{sessionId}")
    int incrementMessageCount(@Param("sessionId") String sessionId, @Param("lastMessageTime") LocalDateTime lastMessageTime);

    @Update("UPDATE chat_session SET status = #{status}, end_time = #{endTime} WHERE session_id = #{sessionId}")
    int updateStatus(@Param("sessionId") String sessionId, @Param("status") Integer status, @Param("endTime") LocalDateTime endTime);

    @Update("UPDATE chat_session SET status = 0, end_time = NOW() WHERE session_id = #{sessionId} AND status = 1")
    int endSession(@Param("sessionId") String sessionId);

    @Update("UPDATE chat_session SET session_data = #{sessionData} WHERE session_id = #{sessionId}")
    int updateSessionData(@Param("sessionId") String sessionId, @Param("sessionData") String sessionData);

    @Delete("DELETE FROM chat_session WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);

    @Delete("DELETE FROM chat_session WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM chat_session WHERE create_time < #{expireTime}")
    int deleteExpired(@Param("expireTime") LocalDateTime expireTime);


    @Insert("""
            INSERT INTO chat_session 
            (session_id, user_id, user_name, session_type, status, start_time, end_time, 
             message_count, last_message_time, session_data) 
            VALUES 
            <foreach collection="list" item="item" separator=",">
                (#{item.sessionId}, #{item.userId}, #{item.userName}, #{item.sessionType}, 
                 #{item.status}, #{item.startTime}, #{item.endTime}, #{item.messageCount}, 
                 #{item.lastMessageTime}, #{item.sessionData})
            </foreach>
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("list") List<ChatSession> sessions);


    @Select("""
            <script>
                SELECT * FROM chat_session 
                <where>
                    <if test="sessionId != null and sessionId != ''">
                        AND session_id = #{sessionId}
                    </if>
                    <if test="userId != null">
                        AND user_id = #{userId}
                    </if>
                    <if test="sessionType != null">
                        AND session_type = #{sessionType}
                    </if>
                    <if test="status != null">
                        AND status = #{status}
                    </if>
                    <if test="startTime != null">
                        AND start_time >= #{startTime}
                    </if>
                    <if test="endTime != null">
                        AND start_time &lt;= #{endTime}
                    </if>
                    <if test="keyword != null and keyword != ''">
                        AND (user_name LIKE CONCAT('%', #{keyword}, '%') OR session_data LIKE CONCAT('%', #{keyword}, '%'))
                    </if>
                </where>
                ORDER BY last_message_time DESC
                LIMIT #{limit}
            </script>
    """)
    @ResultMap("chatSessionMap")
    List<ChatSession> searchSessions(@Param("sessionId") String sessionId,
                                     @Param("userId") Long userId,
                                     @Param("sessionType") Integer sessionType,
                                     @Param("status") Integer status,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("keyword") String keyword,
                                     @Param("limit") int limit);

    @Select("""
            SELECT 
                COUNT(*) as totalSessions,
                SUM(message_count) as totalMessages,
                AVG(message_count) as avg_messagesPerSession,
                AVG(TIMESTAMPDIFF(SECOND, start_time, COALESCE(end_time, NOW()))) as avgDurationSeconds
            FROM chat_session
            WHERE start_time >= #{startTime}
    """)
    Map<String, Object> getOverallStats(@Param("startTime") LocalDateTime startTime);


    @Results(id = "chatSessionMap", value = {
            @Result(property = "id", column = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(property = "sessionId", column = "session_id", jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", jdbcType = JdbcType.BIGINT),
            @Result(property = "userName", column = "user_name", jdbcType = JdbcType.VARCHAR),
            @Result(property = "sessionType", column = "session_type", jdbcType = JdbcType.TINYINT),
            @Result(property = "status", column = "status", jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "endTime", column = "end_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "messageCount", column = "message_count", jdbcType = JdbcType.INTEGER),
            @Result(property = "lastMessageTime", column = "last_message_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "sessionData", column = "session_data", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "createTime", column = "create_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("SELECT * FROM chat_session WHERE id = #{id}")
    ChatSession selectByIdWithMapping(@Param("id") Long id);


    @Select("""
        SELECT 
            HOUR(start_time) as hour,
            COUNT(*) as sessionCount
        FROM chat_session
        WHERE start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
        GROUP BY HOUR(start_time)
        ORDER BY hour
    """)
    List<Map<String, Object>> getHourlyStats();


    @Select("""
            SELECT 
                user_id as userId,
                user_name as userName,
                COUNT(*) as sessionCount,
                SUM(message_count) as totalMessages,
                MAX(last_message_time) as lastActiveTime
            FROM chat_session
            WHERE start_time >= #{startTime}
            GROUP BY user_id, user_name
            ORDER BY session_count DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getTopUsers(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

}
