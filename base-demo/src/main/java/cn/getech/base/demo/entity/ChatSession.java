package cn.getech.base.demo.entity;

import cn.getech.base.demo.enums.ChatSessionStatusEnum;
import cn.getech.base.demo.enums.ChatSessionTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.ChatSessionStatusEnum.*;

/**
 * @author 11030
 */
@Data
@TableName("chat_session")
@Accessors(chain = true)
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("session_type")
    private Integer sessionType;

    @TableField("status")
    private Integer status;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @TableField("message_count")
    private Integer messageCount = 0;

    @TableField("last_message_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    @TableField("session_data")
    private String sessionData;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 计算会话时长（秒）
     */
    public Long getDurationSeconds() {
        if (startTime == null) {
            return 0L;
        }
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end).getSeconds();
    }

    /**
     * 计算会话时长（分钟）
     */
    public Long getDurationMinutes() {
        return getDurationSeconds() / 60;
    }

    /**
     * 判断会话是否活跃
     */
    public boolean isActive() {
        return ACTIVE.equals(status) || WAITING.equals(status);
    }

    /**
     * 判断会话是否已结束
     */
    public boolean isEnded() {
        return ENDED.equals(status);
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sessionId", sessionId);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("sessionType", sessionType);
        map.put("sessionTypeText", ChatSessionTypeEnum.getDescription(sessionType));
        map.put("status", status);
        map.put("statusText", ChatSessionStatusEnum.getDescription(status));
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("messageCount", messageCount);
        map.put("lastMessageTime", lastMessageTime);
        map.put("durationSeconds", getDurationSeconds());
        map.put("durationMinutes", getDurationMinutes());
        map.put("createTime", createTime);
        map.put("updateTime", updateTime);
        map.put("isActive", isActive());
        map.put("isEnded", isEnded());
        return map;
    }

}