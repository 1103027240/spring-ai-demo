package cn.getech.base.demo.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class MessageDocumentVO {

    /**
     * Milvus文档ID
     */
    private String id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 用户ID
     */
    private Double userId;

    /**
     * 消息ID（MySQL中的ID）
     */
    private Double messageId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息类型
     */
    private Double messageType;

    /**
     * 工作流执行ID
     */
    private String workflowExecutionId;

    /**
     * 创建时间
     */
    private Double createTime;

    /**
     * 完整元数据
     */
    private Map<String, Object> metadata;

}
