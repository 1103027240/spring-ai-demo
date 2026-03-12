package cn.getech.base.demo.entity;

import cn.getech.base.demo.enums.WorkflowExecutionStatusEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.WorkflowExecutionStatusEnum.*;

/**
 * @author 11030
 */
@Data
@TableName("workflow_execution")
@Accessors(chain = true)
public class WorkflowExecution {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("execution_id")
    private String executionId;

    @TableField("workflow_name")
    private String workflowName;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("input_data")
    private String inputData;

    @TableField("output_data")
    private String outputData;

    @TableField("status")
    private String status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 计算执行时长
     */
    public Long calculateDuration() {
        if (startTime == null) {
            return 0L;
        }
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMillis();
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return FAILED.equals(status) || TIMEOUT.equals(status);
    }

    /**
     * 是否完成
     */
    public boolean isCompleted() {
        return isSuccess() || isFailed() || CANCELLED.equals(status);
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("executionId", executionId);
        map.put("workflowName", workflowName);
        map.put("sessionId", sessionId);
        map.put("userId", userId);
        map.put("status", status);
        map.put("statusText", WorkflowExecutionStatusEnum.getDescription(status));
        map.put("errorMessage", errorMessage);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("durationMs", durationMs != null ? durationMs : calculateDuration());
        map.put("createTime", createTime);
        return map;
    }

}
