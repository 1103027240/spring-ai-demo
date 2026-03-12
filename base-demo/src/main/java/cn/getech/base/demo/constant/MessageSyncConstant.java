package cn.getech.base.demo.constant;

/**
 * 消息同步相关常量
 * @author 11030
 */
public class MessageSyncConstant {

    /**
     * 同步任务缓存过期时间（秒）
     */
    public static final long SYNC_TASK_CACHE_EXPIRE_SECONDS = 3600L;

    /**
     * Milvus文档元数据字段
     */
    public static final class MetadataField {
        public static final String MESSAGE_ID = "messageId";
        public static final String SESSION_ID = "sessionId";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String MESSAGE_TYPE_TEXT = "messageTypeText";
        public static final String IS_AI = "isAi";
        public static final String WORKFLOW_EXECUTION_ID = "workflowExecutionId";
        public static final String CREATE_TIME = "createTime";
        public static final String SYNC_TIME = "syncTime";
        public static final String SOURCE = "source";
        public static final String RESPONSE_TIME_MS = "responseTimeMs";
        public static final String INTENT = "intent";
        public static final String SENTIMENT = "sentiment";

    }

    /**
     * Milvus文档内容前缀
     */
    public static final class ContentPrefix {
        public static final String MESSAGE_TYPE = "消息类型: ";
        public static final String CONTENT = "\n内容: ";
        public static final String INTENT = "\n意图: ";
        public static final String SENTIMENT = "\n情感: ";

    }

    /**
     * 数据源标识
     */
    public static final String SOURCE_MYSQL_SYNC = "mysql_sync";

}
