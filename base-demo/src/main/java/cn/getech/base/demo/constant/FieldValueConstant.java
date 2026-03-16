package cn.getech.base.demo.constant;

public class FieldValueConstant {

    /**
     * AI回复默认消息
     */
    public static final String DEFAULT_CUSTOMER_AI_RESPONSE = "您好，请问有什么可以帮助您的？";

    public static final String CUSTOMER_SERVICE_MESSAGE_TYPE = "消息类型: ";

    public static final String CUSTOMER_SERVICE_CONTENT = "\n内容: ";

    public static final String CUSTOMER_SERVICE_INTENT = "\n意图: ";

    public static final String CUSTOMER_SERVICE_SENTIMENT = "\n情感: ";

    public static final String NO_ORDERS_FOUND_MESSAGE = "未找到符合条件的订单。";

    public static final String ORDERS_FOUND_TEMPLATE = "找到【%d】个订单：\n";

    public static final String MORE_ORDERS_TEMPLATE = "... 还有【%d】个订单";

    public static final String ORDER_ITEM_TEMPLATE = "%d. %s - %s\n";

    public static final String MESSAGE_ORDER_NOT_FOUND = "未找到订单号，请提供订单号以便处理%s";

    public static final String MESSAGE_ORDER_INVALID = "订单不存在[%s]，请检查订单号是否正确";

    public static final String MESSAGE_APPLICATION_SUBMITTED = "%s申请已提交，服务单号: %s，%s";

    public static final String SOURCE_MYSQL_SYNC = "mysql_sync";

    public static final String CUSTOMER_CURSOR_SEPARATOR = "|"; // 游标字段分隔符

    public static final String DEFAULT_ORDER_DESC = "DESC";

    public static final String DEFAULT_ORDER_ASC = "ASC";

    public static final String STATUS_SUCCESS = "success";

    public static final String STATUS_ERROR = "error";

    public static final String WORKFLOW_CUSTOMER_SERVICE = "customer_service";

    public static final String CUSTOMER_KNOWLEDGE_PREFIX = "customer:knowledge:";

    public static final String CUSTOMER_COLLECTION_NAME = "customer_knowledge";

    public static final String CUSTOMER_CURSOR_PREFIX = "milvus:customer:cursor:";

    public static final String EXPORT_TASK_PREFIX = "milvus:export:task:";

    /**
     * 事务提交后延迟时间（毫秒）
     */
    public static final long TRANSACTION_COMMIT_DELAY_MS = 100L;

    /**
     * 同步任务缓存过期时间（秒）
     */
    public static final long SYNC_TASK_CACHE_EXPIRE_SECONDS = 3600L;

    public static final int DEFAULT_DAYS_RANGE = 30;

    public static final int DEFAULT_LIMIT = 20;

    public static final int SUMMARY_DISPLAY_COUNT = 3;

    public static final int DEFAULT_QUERY_DAYS = 30;

    public static final int DEFAULT_PRODUCT_QUERY_DAYS = 90;

    public static final int DEFAULT_USER_RECENT_LIMIT = 5;

    public static final int RANDOM_NUMBER_BOUND = 99999999;

    public static final int RANDOM_NUMBER_FORMAT_LENGTH = 8;

    public static final int CACHE_TTL = 3600; // 1小时

}
