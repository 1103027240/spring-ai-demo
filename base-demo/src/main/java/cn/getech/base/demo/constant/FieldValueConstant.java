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
     * 工作流名称
     */
    public static final String WORKFLOW_CUSTOMER_SERVICE = "customer_service";

    /**
     * AI回复默认消息
     */
    public static final String DEFAULT_AI_RESPONSE = "您好，请问有什么可以帮助您的？";

    /**
     * AI回复错误消息
     */
    public static final String ERROR_AI_RESPONSE = "抱歉，系统处理您的请求时出现了问题，请稍后重试。";

    /**
     * 事务提交后延迟时间（毫秒）
     */
    public static final long TRANSACTION_COMMIT_DELAY_MS = 100L;

    public static final String INTENT = "intent";

    public static final String SENTIMENT = "sentiment";

    public static final String KNOWLEDGE_CONTEXT = "knowledgeContext";

    public static final String ORDER_RESULTS = "orderResults";

    public static final String AFTER_SALES_RESULT = "afterSalesResult";

}
