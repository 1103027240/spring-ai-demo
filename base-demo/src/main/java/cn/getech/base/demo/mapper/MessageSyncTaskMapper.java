package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.MessageSyncTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.annotations.CacheNamespace;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
@CacheNamespace(flushInterval = 60000, size = 512)
public interface MessageSyncTaskMapper extends BaseMapper<MessageSyncTask> {

    @Select("SELECT * FROM message_sync_task WHERE session_id = #{sessionId} AND status = 0 ORDER BY created_time ASC LIMIT 1")
    @ResultMap("messageSyncTaskMap")
    MessageSyncTask selectLatestPendingTask(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM message_sync_task WHERE session_id = #{sessionId} AND status = 1 ORDER BY created_time DESC LIMIT 1")
    @ResultMap("messageSyncTaskMap")
    MessageSyncTask selectLatestProcessingTask(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM message_sync_task WHERE session_id = #{sessionId} ORDER BY created_time DESC LIMIT #{limit}")
    @ResultMap("messageSyncTaskMap")
    List<MessageSyncTask> selectBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);

    @Select("SELECT * FROM message_sync_task WHERE status = #{status} ORDER BY created_time ASC LIMIT #{limit}")
    @ResultMap("messageSyncTaskMap")
    List<MessageSyncTask> selectByStatus(@Param("status") Integer status, @Param("limit") int limit);

    @Select("SELECT * FROM message_sync_task WHERE status = 3 AND retry_count < max_retries AND created_time >= #{expiryTime} ORDER BY retry_count ASC, created_time ASC")
    @ResultMap("messageSyncTaskMap")
    List<MessageSyncTask> selectFailedTasksBefore(@Param("expiryTime") LocalDateTime expiryTime);

    @Select("SELECT * FROM message_sync_task WHERE status = 3 AND retry_count >= max_retries ORDER BY created_time DESC LIMIT #{limit}")
    @ResultMap("messageSyncTaskMap")
    List<MessageSyncTask> selectPermanentlyFailedTasks(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM message_sync_task WHERE status = 0")
    int countPendingTasks();

    @Select("SELECT COUNT(*) FROM message_sync_task WHERE status = 1")
    int countProcessingTasks();

    @Select("SELECT COUNT(*) FROM message_sync_task WHERE status = 2")
    int countCompletedTasks();

    @Select("SELECT COUNT(*) FROM message_sync_task WHERE status = 3")
    int countFailedTasks();

    @Select("SELECT COUNT(*) FROM message_sync_task WHERE status = 3 AND retry_count >= max_retries")
    int countPermanentlyFailedTasks();


    @Select("""
            SELECT 
                status,
                COUNT(*) as taskCount,
                AVG(retry_count) as avgRetryCount,
                AVG(TIMESTAMPDIFF(SECOND, start_time, COALESCE(end_time, NOW()))) as avgDurationSeconds
            FROM message_sync_task
            WHERE created_time >= #{startTime}
            GROUP BY status
            ORDER BY task_count DESC
    """)
    List<Map<String, Object>> getStatsByStatus(@Param("startTime") LocalDateTime startTime);


    @Select("""
            SELECT 
                sync_type syncType,
                COUNT(*) as taskCount,
                AVG(retry_count) as avgRetryCount,
                AVG(processed_messages) as avgProcessedMessages
            FROM message_sync_task
            WHERE created_time >= #{startTime}
            GROUP BY sync_type
            ORDER BY task_count DESC
    """)
    List<Map<String, Object>> getStatsBySyncType(@Param("startTime") LocalDateTime startTime);


    @Update("UPDATE message_sync_task SET status = #{status}, updated_time = NOW() WHERE id = #{taskId}")
    int updateStatus(@Param("taskId") Long taskId, @Param("status") Integer status);


    @Update("""
            UPDATE message_sync_task 
            SET status = #{status}, 
                retry_count = retry_count + 1, 
                error_message = #{errorMessage}, 
                updated_time = NOW() 
            WHERE id = #{taskId}
    """)
    int updateAsFailed(@Param("taskId") Long taskId, @Param("status") Integer status, @Param("errorMessage") String errorMessage);


    @Update("""
            UPDATE message_sync_task 
            SET status = #{status}, 
                progress = #{progress}, 
                processed_messages = #{processedMessages},
                updated_time = NOW() 
            WHERE id = #{taskId}
    """)
    int updateProgress(@Param("taskId") Long taskId,
                       @Param("status") Integer status,
                       @Param("progress") Integer progress,
                       @Param("processedMessages") Integer processedMessages);


    @Update("""
            UPDATE message_sync_task 
            SET status = 2, 
                end_time = NOW(), 
                progress = 100,
                processed_messages = total_messages,
                updated_time = NOW() 
            WHERE id = #{taskId}
    """)
    int markAsCompleted(@Param("taskId") Long taskId);


    @Update("""
            UPDATE message_sync_task 
            SET start_time = NOW(), 
                status = 1, 
                updated_time = NOW() 
            WHERE id = #{taskId} AND status = 0
    """)
    int startProcessing(@Param("taskId") Long taskId);


    @Delete("DELETE FROM message_sync_task WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);

    @Delete("DELETE FROM message_sync_task WHERE created_time < #{expiryTime}")
    int deleteExpiredTasks(@Param("expiryTime") LocalDateTime expiryTime);

    @Delete("DELETE FROM message_sync_task WHERE status = 2 AND created_time < #{expiryTime}")
    int deleteCompletedExpiredTasks(@Param("expiryTime") LocalDateTime expiryTime);


    @Insert("""
            INSERT INTO message_sync_task 
            (session_id, sync_type, last_message_id, status, retry_count, max_retries, 
             error_message, progress, total_messages, processed_messages, start_time, end_time) 
            VALUES 
            <foreach collection="list" item="item" separator=",">
                (#{item.sessionId}, #{item.syncType}, #{item.lastMessageId}, #{item.status}, 
                 #{item.retryCount}, #{item.maxRetries}, #{item.errorMessage}, #{item.progress}, 
                 #{item.totalMessages}, #{item.processedMessages}, #{item.startTime}, #{item.endTime})
            </foreach>
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("list") List<MessageSyncTask> tasks);


    @Select("""
            <script>
                SELECT * FROM message_sync_task 
                <where>
                    <if test="sessionId != null and sessionId != ''">
                        AND session_id = #{sessionId}
                    </if>
                    <if test="syncType != null">
                        AND sync_type = #{syncType}
                    </if>
                    <if test="status != null">
                        AND status = #{status}
                    </if>
                    <if test="minRetryCount != null">
                        AND retry_count >= #{minRetryCount}
                    </if>
                    <if test="maxRetryCount != null">
                        AND retry_count &lt;= #{maxRetryCount}
                    </if>
                    <if test="startTime != null">
                        AND created_time >= #{startTime}
                    </if>
                    <if test="endTime != null">
                        AND created_time &lt;= #{endTime}
                    </if>
                </where>
                ORDER BY created_time DESC
                LIMIT #{limit}
            </script>
    """)
    @ResultMap("messageSyncTaskMap")
    List<MessageSyncTask> searchTasks(@Param("sessionId") String sessionId,
                                      @Param("syncType") Integer syncType,
                                      @Param("status") Integer status,
                                      @Param("minRetryCount") Integer minRetryCount,
                                      @Param("maxRetryCount") Integer maxRetryCount,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("limit") int limit);


    @Results(id = "messageSyncTaskMap", value = {
            @Result(property = "id", column = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(property = "sessionId", column = "session_id", jdbcType = JdbcType.VARCHAR),
            @Result(property = "syncType", column = "sync_type", jdbcType = JdbcType.TINYINT),
            @Result(property = "lastMessageId", column = "last_message_id", jdbcType = JdbcType.BIGINT),
            @Result(property = "status", column = "status", jdbcType = JdbcType.TINYINT),
            @Result(property = "retryCount", column = "retry_count", jdbcType = JdbcType.INTEGER),
            @Result(property = "maxRetries", column = "max_retries", jdbcType = JdbcType.INTEGER),
            @Result(property = "errorMessage", column = "error_message", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "progress", column = "progress", jdbcType = JdbcType.INTEGER),
            @Result(property = "totalMessages", column = "total_messages", jdbcType = JdbcType.INTEGER),
            @Result(property = "processedMessages", column = "processed_messages", jdbcType = JdbcType.INTEGER),
            @Result(property = "startTime", column = "start_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "endTime", column = "end_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "createdTime", column = "created_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updatedTime", column = "updated_time", jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("SELECT * FROM message_sync_task WHERE id = #{id}")
    MessageSyncTask selectByIdWithMapping(@Param("id") Long id);


    @Select("""
            SELECT 
                DATE(created_time) as date,
                COUNT(*) as totalTasks,
                SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completedTasks,
                SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as failedTasks,
                AVG(processed_messages) as avgProcessedMessages
            FROM message_sync_task
            WHERE created_time >= #{startTime}
            GROUP BY DATE(created_time)
            ORDER BY date DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getDailyTaskStats(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);


    @Select("""
            SELECT 
                session_id sessionId,
                COUNT(*) as taskCount,
                MAX(created_time) as lastTaskTime,
                SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as failedCount
            FROM message_sync_task
            WHERE created_time >= #{startTime}
            GROUP BY session_id
            HAVING failed_count > 0
            ORDER BY failed_count DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getSessionsWithFailedTasks(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

}