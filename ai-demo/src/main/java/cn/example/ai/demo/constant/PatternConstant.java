package cn.example.ai.demo.constant;

import java.util.regex.Pattern;

public class PatternConstant {

    // 订单号正则表达式
    public static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{2,6}\\d{6,12})|(\\d{6,12})");

    // 服务单号正则表达式
    public static final Pattern SERVICE_NUMBER_PATTERN = Pattern.compile("[A-Z]{2}\\d{8,12}");

    public static final Pattern QUERY_TYPE_PATTERN = Pattern.compile("\"queryType\"\\s*:\\s*\"([^\"]+)\"");

    public static final Pattern QUERY_ORDER_NUMBER_PATTERN = Pattern.compile("\"orderNumber\"\\s*:\\s*\"([^\"]*)\"");

    public static final Pattern QUERY_ORDER_STATUS_PATTERN = Pattern.compile("\"orderStatus\"\\s*:\\s*\"([^\"]*)\"");

    public static final Pattern QUERY_PRODUCT_INFO_PATTERN = Pattern.compile("\"productInfo\"\\s*:\\s*\"([^\"]*)\"");

    public static final Pattern MARKDOWN_CODE_BLOCK_PATTERN = Pattern.compile("^(?i)```(?:json)?\\s*|```\\s*$", Pattern.MULTILINE);

    // SQL注入检测模式
    public static final Pattern SQL_INJECTION_PATTERN =
            Pattern.compile("(?i)(\\b(INSERT\\s+INTO|UPDATE\\s+\\w+|DELETE\\s+FROM|DROP\\s+TABLE|TRUNCATE\\s+TABLE|ALTER\\s+TABLE|CREATE\\s+TABLE)\\b)");

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

}
