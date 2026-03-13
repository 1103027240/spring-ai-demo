package cn.getech.base.demo.constant;

import java.util.regex.Pattern;

/**
 * @author 11030
 */
public class FieldValueConstant {

    // 订单号正则表达式
    public static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{2,6}\\d{6,12})|(\\d{6,12})");

    // 服务单号正则表达式
    public static final Pattern SERVICE_NUMBER_PATTERN = Pattern.compile("[A-Z]{2}\\d{8,12}");

    /**
     * AI回复可能的键名
     */
    public static final String[] AI_RESPONSE_KEYS = {
            "aiResponse",
            "response",
            "answer",
            "content",
            "message"
    };



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

    /**
     * 工作流名称
     */
    public static final String WORKFLOW_CUSTOMER_SERVICE = "customer_service";

    /**
     * 数据源标识
     */
    public static final String SOURCE_MYSQL_SYNC = "mysql_sync";

    public static final String CUSTOMER_SERVICE_MESSAGE_TYPE = "消息类型: ";

    public static final String CUSTOMER_SERVICE_CONTENT = "\n内容: ";

    public static final String CUSTOMER_SERVICE_INTENT = "\n意图: ";

    public static final String CUSTOMER_SERVICE_SENTIMENT = "\n情感: ";



    public static final String MESSAGE_ID = "messageId";

    public static final String USER_ID = "userId";

    public static final String USER_NAME = "userName";

    public static final String USER_INPUT = "userInput";

    public static final String SESSION_ID = "sessionId";

    public static final String MESSAGE_TYPE = "messageType";

    public static final String IS_AI = "isAi";

    public static final String AI_RESPONSE = "aiResponse";

    public static final String WORKFLOW_EXECUTION_ID = "workflowExecutionId";

    public static final String INTENT = "intent";

    public static final String SENTIMENT = "sentiment";

    public static final String KNOWLEDGE_RESULTS = "knowledgeResults";

    public static final String KNOWLEDGE_CONTEXT = "knowledgeContext";

    public static final String ORDER_RESULTS = "orderResults";

    public static final String AFTER_SALES_RESULT = "afterSalesResult";

    public static final String CREATE_TIME = "createTime";

    public static final String SYNC_TIME = "syncTime";

    public static final String TIME_STAMP = "timestamp";

    public static final String RESPONSE_TIME_MS = "responseTimeMs";

    public static final String DURATION_MS = "durationMs";

    public static final String INTENT_RECOGNITION_TIME = "intentRecognitionTime";

    public static final String SENTIMENT_ANALYSIS_TIME = "sentimentAnalysisTime";

    public static final String AFTER_SALES_TIME = "afterSalesTime";

    public static final String ORDER_QUERY_TIME = "orderQueryTime";

    public static final String KNOWLEDGE_RETRIEVAL_TIME = "knowledgeRetrievalTime";

    public static final String RESPONSE_GENERATION_TIME = "responseGenerationTime";

    public static final String SOURCE = "source";

    public static final String RESULT = "result";

    public static final String RESULTS = "results";

    public static final String MESSAGE = "message";

    public static final String STATUS = "status";

    public static final String WORKFLOW_STATUS = "workflowStatus";

    public static final String EXECUTION_PATH = "executionPath";

    public static final String SENTIMENT_INTENSITY = "sentimentIntensity";

    public static final String AFTER_SALES_TYPE = "afterSalesType";

    public static final String AFTER_SALES_ANALYSIS = "afterSalesAnalysis";

    public static final String PRIORITY = "priority";

    public static final String COMPLAINT_ID = "complaintId";

    public static final String CONTENT = "content";

    public static final String ORDER_ID = "orderId";

    public static final String ORDER_NUMBER = "orderNumber";

    public static final String USER_INFO = "userInfo";

    public static final String QUERY_TYPE = "queryType";

    public static final String ORDER_EXTRACTION = "orderExtraction";

    public static final String STATUS_DESCRIPTION = "statusDescription";

    public static final String STATUS_DETAIL_DESCRIPTION = "statusDetailDescription";

    public static final String PAYMENT_METHOD = "paymentMethod";

    public static final String TOTAL_AMOUNT = "totalAmount";

    public static final String SHIPPING_ADDRESS = "shippingAddress";

    public static final String CONTACT_PHONE = "contactPhone";

    public static final String TITLE = "title";

    public static final String SCORE = "score";

    public static final String CATEGORY = "category";

    public static final String TAGS = "tags";

}
