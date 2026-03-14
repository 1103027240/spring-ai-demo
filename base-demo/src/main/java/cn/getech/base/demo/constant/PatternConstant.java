package cn.getech.base.demo.constant;

import java.util.regex.Pattern;

public class PatternConstant {

    // 订单号正则表达式
    public static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{2,6}\\d{6,12})|(\\d{6,12})");

    // 服务单号正则表达式
    public static final Pattern SERVICE_NUMBER_PATTERN = Pattern.compile("[A-Z]{2}\\d{8,12}");

    public static final Pattern QUERY_TYPE_PATTERN = Pattern.compile("\"queryType\"\\s*:\\s*\"([^\"]+)\"");

    public static final Pattern QUERY_ORDER_NUMBER_PATTERN = Pattern.compile("\"orderNumber\"\\s*:\\s*\"([^\"]*)\"");

    public static final Pattern ORDER_STATUS_PATTERN = Pattern.compile("\"orderStatus\"\\s*:\\s*\"([^\"]*)\"");

    public static final Pattern PRODUCT_INFO_PATTERN = Pattern.compile("\"productInfo\"\\s*:\\s*\"([^\"]*)\"");

    public static final Pattern MARKDOWN_CODE_BLOCK_PATTERN = Pattern.compile("^(?i)```(?:json)?\\s*|```\\s*$", Pattern.MULTILINE);

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
