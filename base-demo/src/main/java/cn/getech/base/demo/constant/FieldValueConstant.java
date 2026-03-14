package cn.getech.base.demo.constant;

public class FieldValueConstant {

    /**
     * 事务提交后延迟时间（毫秒）
     */
    public static final long TRANSACTION_COMMIT_DELAY_MS = 100L;

    /**
     * 同步任务缓存过期时间（秒）
     */
    public static final long SYNC_TASK_CACHE_EXPIRE_SECONDS = 3600L;

    /**
     * AI回复默认消息
     */
    public static final String DEFAULT_AI_RESPONSE = "您好，请问有什么可以帮助您的？";

    public static final String CUSTOMER_SERVICE_MESSAGE_TYPE = "消息类型: ";

    public static final String CUSTOMER_SERVICE_CONTENT = "\n内容: ";

    public static final String CUSTOMER_SERVICE_INTENT = "\n意图: ";

    public static final String CUSTOMER_SERVICE_SENTIMENT = "\n情感: ";

    /**
     * 工作流名称
     */
    public static final String WORKFLOW_CUSTOMER_SERVICE = "customer_service";

    /**
     * 数据源标识
     */
    public static final String SOURCE_MYSQL_SYNC = "mysql_sync";

    public static final int DEFAULT_DAYS_RANGE = 30;

    public static final int DEFAULT_LIMIT = 20;

    public static final int SUMMARY_DISPLAY_COUNT = 3;

    public static final String DEFAULT_ORDER_DESC = "DESC";

    // 摘要消息常量
    public static final String NO_ORDERS_FOUND_MESSAGE = "未找到符合条件的订单。";

    public static final String ORDERS_FOUND_TEMPLATE = "找到【%d】个订单：\n";

    public static final String MORE_ORDERS_TEMPLATE = "... 还有【%d】个订单";

    public static final String ORDER_ITEM_TEMPLATE = "%d. %s - %s\n";

}
