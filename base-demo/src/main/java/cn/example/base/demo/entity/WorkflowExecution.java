package cn.example.base.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import static cn.example.base.demo.enums.WorkflowExecutionStatusEnum.*;

/**
 * @author 11030
 */
@Data
@TableName("workflow_execution")
@Accessors(chain = true)
public class WorkflowExecution implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 计算执行时长
     */
    public Long calculateDuration() {
        if (startTime == null) {
            return 0L;
        }
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end).toMillis();
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

}
