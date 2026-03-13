package cn.getech.base.demo.entity;

import cn.getech.base.demo.enums.ChatMessageSyncStatusEnum;
import cn.getech.base.demo.enums.ChatMessageTypeEnum;
import cn.getech.base.demo.enums.SentimentAnalysisEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.ChatMessageSyncStatusEnum.FAILED;
import static cn.getech.base.demo.enums.ChatMessageSyncStatusEnum.SYNCED;

/**
 * @author 11030
 */
@Data
@TableName("chat_message")
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("message_type")
    private Integer messageType;

    private String content;

    private String intent;

    private String sentiment;

    /**
     * 是否为AI回复：0-否, 1-是
     */
    @TableField("is_ai_response")
    private Integer isAiResponse = 0;

    @TableField("workflow_execution_id")
    private String workflowExecutionId;

    @TableField("sync_status")
    private Integer syncStatus = 0;

    @TableField("sync_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime syncTime;

    @TableField("response_time")
    private Integer responseTime;

    /**
     * 是否已删除：0-否, 1-是
     */
    @TableField("is_deleted")
    private Integer isDeleted = 0;

    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 是否为AI消息
     */
    public boolean isAiMessage() {
        return isAiResponse != null && isAiResponse == 1;
    }

    /**
     * 是否为用户消息
     */
    public boolean isUserMessage() {
        return !isAiMessage();
    }

    /**
     * 是否已同步
     */
    public boolean isSynced() {
        return SYNCED.equals(syncStatus);
    }

    /**
     * 是否同步失败
     */
    public boolean isSyncFailed() {
        return FAILED.equals(syncStatus);
    }

    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sessionId", sessionId);
        map.put("userId", userId);
        map.put("messageType", messageType);
        map.put("messageTypeText", ChatMessageTypeEnum.getDescription(messageType));
        map.put("content", content);
        map.put("contentLength", content != null ? content.length() : 0);
        map.put("intent", intent);
        map.put("sentiment", sentiment);
        map.put("sentimentText", SentimentAnalysisEnum.getDescription(sentiment));
        map.put("isAi", isAiMessage());
        map.put("isUser", isUserMessage());
        map.put("workflowExecutionId", workflowExecutionId);
        map.put("syncStatus", syncStatus);
        map.put("syncStatusText", ChatMessageSyncStatusEnum.getDescription(syncStatus));
        map.put("syncTime", syncTime);
        map.put("responseTime", responseTime);
        map.put("isDeleted", isDeleted);
        map.put("isSynced", isSynced());
        map.put("isSyncFailed", isSyncFailed());
        map.put("createTime", createTime);
        return map;
    }

    /**
     * 获取消息摘要（前50个字符）
     */
    public String getSummary() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        int length = Math.min(content.length(), 50);
        return content.substring(0, length) + (content.length() > 50 ? "..." : "");
    }

}
