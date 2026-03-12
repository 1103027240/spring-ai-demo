package cn.getech.base.demo.constant;

/**
 * 聊天消息相关常量
 * @author 11030
 */
public class ChatMessageConstant {

    /**
     * AI响应标识 - 否
     */
    public static final int IS_AI_RESPONSE_NO = 0;

    /**
     * AI响应标识 - 是
     */
    public static final int IS_AI_RESPONSE_YES = 1;

    /**
     * 响应时间转换因子（毫秒转秒）
     */
    public static final int MILLISECONDS_TO_SECONDS = 1000;

    /**
     * 消息字段映射 - ID
     */
    public static final String FIELD_ID = "id";

    /**
     * 消息字段映射 - 会话ID
     */
    public static final String FIELD_SESSION_ID = "sessionId";

    /**
     * 消息字段映射 - 消息类型
     */
    public static final String FIELD_MESSAGE_TYPE = "messageType";

    /**
     * 消息字段映射 - 内容
     */
    public static final String FIELD_CONTENT = "content";

    /**
     * 消息字段映射 - 意图
     */
    public static final String FIELD_INTENT = "intent";

    /**
     * 消息字段映射 - 情感
     */
    public static final String FIELD_SENTIMENT = "sentiment";

    /**
     * 消息字段映射 - 是否AI回复
     */
    public static final String FIELD_IS_AI = "isAi";

    /**
     * 消息字段映射 - 工作流执行ID
     */
    public static final String FIELD_WORKFLOW_EXECUTION_ID = "workflowExecutionId";

    /**
     * 消息字段映射 - 创建时间
     */
    public static final String FIELD_CREATE_TIME = "createTime";

    /**
     * 消息字段映射 - 同步状态
     */
    public static final String FIELD_SYNC_STATUS = "syncStatus";

    private ChatMessageConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
