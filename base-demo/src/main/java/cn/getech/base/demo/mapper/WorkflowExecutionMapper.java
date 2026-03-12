package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.WorkflowExecution;
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
public interface WorkflowExecutionMapper extends BaseMapper<WorkflowExecution> {

    @Select("SELECT * FROM workflow_execution WHERE execution_id = #{executionId}")
    @ResultMap("workflowExecutionMap")
    WorkflowExecution selectByExecutionId(@Param("executionId") String executionId);

    @Select("SELECT * FROM workflow_execution WHERE session_id = #{sessionId} ORDER BY start_time DESC LIMIT #{limit}")
    @ResultMap("workflowExecutionMap")
    List<WorkflowExecution> selectBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);

    @Select("SELECT * FROM workflow_execution WHERE user_id = #{userId} ORDER BY start_time DESC LIMIT #{limit}")
    @ResultMap("workflowExecutionMap")
    List<WorkflowExecution> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM workflow_execution WHERE status = #{status} ORDER BY start_time DESC LIMIT #{limit}")
    @ResultMap("workflowExecutionMap")
    List<WorkflowExecution> selectByStatus(@Param("status") String status, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM workflow_execution WHERE session_id = #{sessionId}")
    int countBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT COUNT(*) FROM workflow_execution WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM workflow_execution WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Select("SELECT COUNT(*) FROM workflow_execution WHERE status = #{status} AND start_time >= #{startTime}")
    int countByStatusAndTime(@Param("status") String status, @Param("startTime") LocalDateTime startTime);


    @Select("""
            SELECT 
                status,
                COUNT(*) as count,
                AVG(duration_ms) as avgDuration,
                MIN(duration_ms) as minDuration,
                MAX(duration_ms) as maxDuration
            FROM workflow_execution
            WHERE start_time >= #{startTime}
            GROUP BY status
            ORDER BY count DESC
        """)
    List<Map<String, Object>> getStatsByStatus(@Param("startTime") LocalDateTime startTime);


    @Select("""
            SELECT 
                DATE(start_time) as date,
                COUNT(*) as totalCount,
                SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount,
                SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failedCount,
                AVG(duration_ms) as avgDuration
            FROM workflow_execution
            WHERE start_time >= #{startTime}
            GROUP BY DATE(start_time)
            ORDER BY date DESC
            LIMIT #{limit}
    """)
    List<Map<String, Object>> getDailyStats(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);


    @Update("""
        UPDATE workflow_execution 
        SET status = #{status}, 
            end_time = #{endTime}, 
            duration_ms = #{durationMs}, 
            output_data = #{outputData}, 
            error_message = #{errorMessage} 
        WHERE execution_id = #{executionId}
    """)
    int updateExecution(@Param("executionId") String executionId,
                        @Param("status") String status,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("durationMs") Long durationMs,
                        @Param("outputData") String outputData,
                        @Param("errorMessage") String errorMessage);


    @Update("UPDATE workflow_execution SET status = #{status} WHERE execution_id = #{executionId}")
    int updateStatus(@Param("executionId") String executionId, @Param("status") String status);

    @Update("UPDATE workflow_execution SET error_message = #{errorMessage} WHERE execution_id = #{executionId}")
    int updateErrorMessage(@Param("executionId") String executionId, @Param("errorMessage") String errorMessage);

    @Delete("DELETE FROM workflow_execution WHERE execution_id = #{executionId}")
    int deleteByExecutionId(@Param("executionId") String executionId);

    @Delete("DELETE FROM workflow_execution WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);

    @Delete("DELETE FROM workflow_execution WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM workflow_execution WHERE start_time < #{expireTime}")
    int deleteExpired(@Param("expireTime") LocalDateTime expireTime);


    @Insert("""
            INSERT INTO workflow_execution 
            (execution_id, workflow_name, session_id, user_id, input_data, output_data, 
             status, error_message, start_time, end_time, duration_ms) 
            VALUES 
            <foreach collection="list" item="item" separator=",">
                (#{item.executionId}, #{item.workflowName}, #{item.sessionId}, #{item.userId}, 
                 #{item.inputData}, #{item.outputData}, #{item.status}, #{item.errorMessage}, 
                 #{item.startTime}, #{item.endTime}, #{item.durationMs})
            </foreach>
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("list") List<WorkflowExecution> executions);


    @Select("""
            <script>
                SELECT * FROM workflow_execution 
                <where>
                    <if test="sessionId != null and sessionId != ''">
                        AND session_id = #{sessionId}
                    </if>
                    <if test="userId != null">
                        AND user_id = #{userId}
                    </if>
                    <if test="status != null and status != ''">
                        AND status = #{status}
                    </if>
                    <if test="startTime != null">
                        AND start_time >= #{startTime}
                    </if>
                    <if test="endTime != null">
                        AND start_time &lt;= #{endTime}
                    </if>
                </where>
                ORDER BY start_time DESC
                LIMIT #{limit}
            </script>
    """)
    @ResultMap("workflowExecutionMap")
    List<WorkflowExecution> searchExecutions(@Param("sessionId") String sessionId,
                                             @Param("userId") Long userId,
                                             @Param("status") String status,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("limit") int limit);


    @Select("""
            SELECT 
                workflow_name as workflowName,
                COUNT(*) as executionCount,
                AVG(duration_ms) as avgDuration,
                SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount,
                SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failedCount
            FROM workflow_execution
            WHERE start_time >= #{startTime}
            GROUP BY workflow_name
            ORDER BY execution_count DESC
    """)
    List<Map<String, Object>> getWorkflowStats(@Param("startTime") LocalDateTime startTime);


    @Results(id = "workflowExecutionMap", value = {
            @Result(property = "id", column = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(property = "executionId", column = "execution_id", jdbcType = JdbcType.VARCHAR),
            @Result(property = "workflowName", column = "workflow_name", jdbcType = JdbcType.VARCHAR),
            @Result(property = "sessionId", column = "session_id", jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", jdbcType = JdbcType.BIGINT),
            @Result(property = "inputData", column = "input_data", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "outputData", column = "output_data", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "status", column = "status", jdbcType = JdbcType.VARCHAR),
            @Result(property = "errorMessage", column = "error_message", jdbcType = JdbcType.LONGVARCHAR),
            @Result(property = "startTime", column = "start_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "endTime", column = "end_time", jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "durationMs", column = "duration_ms", jdbcType = JdbcType.BIGINT),
            @Result(property = "createTime", column = "create_time", jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("SELECT * FROM workflow_execution WHERE id = #{id}")
    WorkflowExecution selectByIdWithMapping(@Param("id") Long id);


    @Select("""
            SELECT 
                COUNT(*) as total,
                MIN(start_time) as earliest,
                MAX(start_time) as latest,
                AVG(duration_ms) as avgDuration
            FROM workflow_execution
            WHERE start_time >= #{startTime}
    """)
    Map<String, Object> getOverallStats(@Param("startTime") LocalDateTime startTime);


    @Select("""
            SELECT 
                HOUR(start_time) as hour,
                COUNT(*) as executionCount
            FROM workflow_execution
            WHERE start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            GROUP BY HOUR(start_time)
            ORDER BY hour
    """)
    List<Map<String, Object>> getHourlyStats();


    @Flush
    void flush();

}