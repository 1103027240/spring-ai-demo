package cn.getech.base.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

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

    @TableField("content")
    private String content;

    @TableField("intent")
    private String intent;

    @TableField("sentiment")
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

    @TableField("metadata")
    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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
