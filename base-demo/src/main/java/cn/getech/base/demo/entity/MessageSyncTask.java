package cn.getech.base.demo.entity;

import cn.getech.base.demo.enums.MessageTaskStatusEnum;
import cn.getech.base.demo.enums.MessageTaskSyncTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.MessageTaskStatusEnum.*;

/**
 * @author 11030
 */
@Data
@TableName("message_sync_task")
@Accessors(chain = true)
public class MessageSyncTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("sync_type")
    private Integer syncType = 1;

    @TableField("last_message_id")
    private Long lastMessageId;

    private Integer status = 0;

    @TableField("retry_count")
    private Integer retryCount = 0;

    @TableField("max_retries")
    private Integer maxRetries = 3;

    @TableField("error_message")
    private String errorMessage;

    private Integer progress = 0;

    @TableField("total_messages")
    private Integer totalMessages = 0;

    @TableField("processed_messages")
    private Integer processedMessages = 0;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @TableField("created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 计算执行时长（毫秒）
     */
    public Long getDurationMs() {
        if (startTime == null) {
            return 0L;
        }
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end).toMillis();
    }

    /**
     * 计算执行时长（秒）
     */
    public Long getDurationSeconds() {
        return getDurationMs() / 1000;
    }

    /**
     * 计算进度百分比
     */
    public Integer getProgressPercentage() {
        if (totalMessages == 0 || processedMessages == 0) {
            return 0;
        }
        return (int) Math.round((double) processedMessages / totalMessages * 100);
    }

    /**
     * 是否需要重试
     */
    public boolean needRetry() {
        return FAILED.equals(status) && retryCount < maxRetries;
    }

    /**
     * 是否可重试
     */
    public boolean canRetry() {
        return needRetry() && !isExpired();
    }

    /**
     * 是否已过期（超过24小时）
     */
    public boolean isExpired() {
        if (createdTime == null) {
            return false;
        }
        Duration duration = Duration.between(createdTime, LocalDateTime.now());
        return duration.toHours() > 24;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return COMPLETED.equals(status);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return FAILED.equals(status);
    }

    /**
     * 是否处理中
     */
    public boolean isProcessing() {
        return PROCESSING.equals(status);
    }

    /**
     * 是否待处理
     */
    public boolean isPending() {
        return PENDING.equals(status);
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sessionId", sessionId);
        map.put("syncType", syncType);
        map.put("syncTypeText", MessageTaskSyncTypeEnum.getDescription(syncType));
        map.put("lastMessageId", lastMessageId);
        map.put("status", status);
        map.put("statusText", MessageTaskStatusEnum.getDescription(status));
        map.put("retryCount", retryCount);
        map.put("maxRetries", maxRetries);
        map.put("errorMessage", errorMessage);
        map.put("progress", progress);
        map.put("progressPercentage", getProgressPercentage());
        map.put("totalMessages", totalMessages);
        map.put("processedMessages", processedMessages);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("durationMs", getDurationMs());
        map.put("durationSeconds", getDurationSeconds());
        map.put("createdTime", createdTime);
        map.put("updatedTime", updatedTime);
        map.put("needRetry", needRetry());
        map.put("canRetry", canRetry());
        map.put("isExpired", isExpired());
        map.put("isCompleted", isCompleted());
        map.put("isFailed", isFailed());
        map.put("isProcessing", isProcessing());
        map.put("isPending", isPending());
        return map;
    }

}
