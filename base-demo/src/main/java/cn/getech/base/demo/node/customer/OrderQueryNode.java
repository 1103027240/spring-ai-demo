package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.OrderService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

/**
 * 订单查询节点
 * @author 11030
 */
@Slf4j
@Component
public class OrderQueryNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("【订单查询节点】开始执行");

        OrderService orderService = SpringUtil.getBean(OrderService.class);
        String userInput = state.value(USER_INPUT, String.class).orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));
        Long userId = state.value(USER_ID, Long.class).orElse(null);

        // 1. 调用大模型提取订单信息
        String extractionResult = extractOrderInfo(userInput);

        // 2. 解析提取订单结果（解析JSON字符串）
        Map<String, Object> orderInfo = parseExtractionResult(extractionResult, userId);

        // 3. 处理时间范围
        processTimeRange(orderInfo);

        // 4. 构建查询参数
        Map<String, Object> queryParams = buildQueryParams(orderInfo, userId);

        // 5. 组装返回结果
        List<Map<String, Object>> orders = orderService.queryOrders(queryParams);
        log.info("【订单查询节点】查询完成，找到[{}]条数据", orders.size());

        // 6. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put(ORDER_EXTRACTION, orderInfo);
        result.put(ORDER_RESULTS, orders);
        result.put(ORDER_QUERY_TIME, System.currentTimeMillis());

        // 7. 添加查询摘要
        addQuerySummary(result, orderInfo, orders);
        return result;
    }

    /**
     * 从用户输入中提取订单信息
     */
    private String extractOrderInfo(String userInput) {
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        String extractPrompt = """
            请从以下用户输入中提取订单相关信息，并严格按照以下JSON格式返回结果：
                    
            {
               "queryType": "BY_ORDER_NUMBER|BY_USER_RECENT|BY_STATUS|BY_TIME_RANGE|BY_PRODUCT|COMPLEX_QUERY",
               "orderNumber": "订单号，如ORD202403080001，没有则为null",
               "orderStatus": "订单状态代码，0-6的整数，没有则为null",
               "timeRange": {
                 "startTime": "开始时间，格式: yyyy-MM-dd HH:mm:ss，没有则为null",
                 "endTime": "结束时间，格式: yyyy-MM-dd HH:mm:ss，没有则为null"
               },
               "productInfo": "商品关键词，没有则为null",
               "extractedKeywords": ["提取的关键词列表"],
               "userIntentDescription": "用户意图的详细描述"
            }
                
            规则说明：
            1. queryType根据以下情况确定：
               - 提到具体订单号 -> BY_ORDER_NUMBER
               - 提到订单状态 -> BY_STATUS
               - 提到时间范围 -> BY_TIME_RANGE
               - 提到商品信息 -> BY_PRODUCT
               - 同时满足多个条件 -> COMPLEX_QUERY
               - 只说"我的订单"等 -> BY_USER_RECENT
            
            2. 时间范围处理：
               - "今天" -> 今天0点到当前时间
               - "昨天" -> 昨天0点到23:59:59
               - "最近7天" -> 7天前到现在
               - "上周" -> 上周一到上周日
               - "本月" -> 本月1号到现在
               - "上月" -> 上月1号到上月最后一天
               - 格式统一为：yyyy-MM-dd HH:mm:ss
            
            3. 订单状态映射（请使用数字代码）：
               - 待支付/未支付/等待支付 -> 0
               - 已支付/支付成功/已付款 -> 1
               - 已发货/发货中/已出库 -> 2
               - 已完成/已签收/交易完成 -> 3
               - 已取消/订单取消/取消的 -> 4
               - 退款中/正在退款/退款处理中 -> 5
               - 已退款/退款完成 -> 6
           
            4. 如果用户提到状态关键词但无法确定具体状态，请记录提取到的关键词，但order_status设为null
            
            请确保返回有效的JSON格式，不要包含任何其他内容。
            
            用户输入：%s
                """.formatted(userInput);

        // 调用大模型提取订单信息（JSON字符串）
        String extractionResult = qwenChatClient.prompt()
                .user(extractPrompt)
                .call()
                .content()
                .trim();

        log.info("【订单查询节点】订单信息提取结果: {}", extractionResult);
        return extractionResult;
    }

    /**
     * 解析大模型返回的提取结果
     */
    private Map<String, Object> parseExtractionResult(String extractionResult, Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (StrUtil.isBlank(extractionResult)) {
            log.warn("【订单查询节点】提取结果为空，使用默认值");
            return getDefaultExtractionResult(userId);
        }

        // 移除 markdown 代码块标记
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        String cleanedResult = cleanMarkdownCodeBlock(extractionResult);

        try {
            // 尝试使用Jackson进行标准的JSON解析
            Map<String, Object> parsedMap = objectMapper.readValue(cleanedResult, new TypeReference<>() {});
            result.putAll(parsedMap);
            log.debug("【订单查询节点】标准JSON解析成功");
        } catch (Exception e) {
            log.warn("【订单查询节点】标准JSON解析失败，使用简化提取: {}, 原因: {}", cleanedResult, e.getMessage());
            result = parseExtractionResultSimple(cleanedResult);
        }

        // 添加用户ID
        result.put(USER_ID, userId);

        // 确保必要字段存在
        ensureRequiredFields(result);

        log.info("【订单查询节点】解析订单提取结果: {}", result);
        return result;
    }

    /**
     * 处理时间范围
     */
    private void processTimeRange(Map<String, Object> orderInfo) {
        if (orderInfo.containsKey(TIME_RANGE) && orderInfo.get(TIME_RANGE) != null) {
            try {
                Map<String, Object> timeRange = (Map<String, Object>) orderInfo.get(TIME_RANGE);

                // 转换时间字符串为LocalDateTime
                if (timeRange.containsKey(START_TIME) && timeRange.get(START_TIME) instanceof String) {
                    String startTimeStr = (String) timeRange.get(START_TIME);
                    if (StrUtil.isNotBlank(startTimeStr) && !"null".equalsIgnoreCase(startTimeStr)) {
                        orderInfo.put(START_TIME, LocalDateTime.parse(startTimeStr, NORM_DATETIME_FORMATTER));
                    }
                }

                if (timeRange.containsKey(END_TIME) && timeRange.get(END_TIME) instanceof String) {
                    String endTimeStr = (String) timeRange.get(END_TIME);
                    if (StrUtil.isNotBlank(endTimeStr) && !"null".equalsIgnoreCase(endTimeStr)) {
                        orderInfo.put(END_TIME, LocalDateTime.parse(endTimeStr, NORM_DATETIME_FORMATTER));
                    }
                }

            } catch (Exception e) {
                log.warn("【订单查询节点】处理时间范围失败: {}", e.getMessage());
            }
        }

        // 设置默认时间范围
        if (!orderInfo.containsKey(START_TIME) || orderInfo.get(START_TIME) == null) {
            orderInfo.put(START_TIME, LocalDateTime.now().minusDays(30));
        }
        if (!orderInfo.containsKey(END_TIME) || orderInfo.get(END_TIME) == null) {
            orderInfo.put(END_TIME, LocalDateTime.now());
        }
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(Map<String, Object> orderInfo, Long userId) {
        Map<String, Object> queryParams = new HashMap<>();

        // 用户ID
        if (userId != null) {
            queryParams.put(USER_ID, userId);
        }

        // 订单号
        if (orderInfo.containsKey(ORDER_NUMBER) && orderInfo.get(ORDER_NUMBER) != null) {
            String orderNumber = (String) orderInfo.get(ORDER_NUMBER);
            if (StrUtil.isNotBlank(orderNumber) && !"null".equalsIgnoreCase(orderNumber)) {
                queryParams.put(ORDER_NUMBER, orderNumber);
            }
        }

        // 订单状态
        if (orderInfo.containsKey(ORDER_STATUS) && orderInfo.get(ORDER_STATUS) != null) {
            String orderStatus = (String) orderInfo.get(ORDER_STATUS);
            if (StrUtil.isNotBlank(orderStatus) && !"null".equalsIgnoreCase(orderStatus)) {
                queryParams.put(STATUS, orderStatus);
            }
        }

        // 商品信息
        if (orderInfo.containsKey(PRODUCT_INFO) && orderInfo.get(PRODUCT_INFO) != null) {
            String productInfo = (String) orderInfo.get(PRODUCT_INFO);
            if (StrUtil.isNotBlank(productInfo) && !"null".equalsIgnoreCase(productInfo)) {
                queryParams.put(PRODUCT_KEYWORD, productInfo);
            }
        }

        // 时间范围
        if (orderInfo.containsKey(START_TIME) && orderInfo.get(START_TIME) != null) {
            queryParams.put(START_TIME, orderInfo.get(START_TIME));
        }
        if (orderInfo.containsKey(END_TIME) && orderInfo.get(END_TIME) != null) {
            queryParams.put(END_TIME, orderInfo.get(END_TIME));
        }

        // 查询类型
        if (orderInfo.containsKey(QUERY_TYPE) && orderInfo.get(QUERY_TYPE) != null) {
            queryParams.put(QUERY_TYPE, orderInfo.get(QUERY_TYPE));
        }

        // 设置默认限制
        queryParams.put(LIMIT, 20);

        log.info("【订单查询节点】构建查询参数: {}", queryParams);
        return queryParams;
    }

    /**
     * 添加查询摘要
     */
    private void addQuerySummary(Map<String, Object> result, Map<String, Object> orderInfo, List<Map<String, Object>> orders) {
        Map<String, Object> summary = new HashMap<>();

        // 查询条件
        summary.put(QUERY_TYPE, orderInfo.get(QUERY_TYPE));
        summary.put(USER_INTENT, orderInfo.get(USER_INTENT_DESCRIPTION));

        // 查询结果
        summary.put(ORDER_COUNT, orders.size());
        summary.put(HAS_RESULTS, CollUtil.isNotEmpty(orders));

        // 提取的关键词
        if (orderInfo.containsKey(EXTRACTED_KEYWORDS)) {
            summary.put(EXTRACTED_KEYWORDS, orderInfo.get(EXTRACTED_KEYWORDS));
        }

        // 添加时间范围
        if (orderInfo.containsKey(TIME_RANGE) && orderInfo.get(TIME_RANGE) != null) {
            summary.put(TIME_RANGE, orderInfo.get(TIME_RANGE));
        }

        result.put(QUERY_SUMMARY, summary);
    }

    /**
     * 确保必要字段存在
     */
    private void ensureRequiredFields(Map<String, Object> result) {
        if (!result.containsKey(QUERY_TYPE) || result.get(QUERY_TYPE) == null) {
            result.put(QUERY_TYPE, "BY_USER_RECENT");
        }

        if (!result.containsKey(TIME_RANGE) || result.get(TIME_RANGE) == null) {
            Map<String, Object> timeRange = new HashMap<>();
            LocalDateTime now = LocalDateTime.now();
            timeRange.put(START_TIME, now.minusDays(30).format(NORM_DATETIME_FORMATTER));
            timeRange.put(END_TIME, now.format(NORM_DATETIME_FORMATTER));
            result.put(TIME_RANGE, timeRange);
        }

        if (!result.containsKey(EXTRACTED_KEYWORDS) || result.get(EXTRACTED_KEYWORDS) == null) {
            result.put(EXTRACTED_KEYWORDS, List.of());
        }

        if (!result.containsKey(USER_INTENT_DESCRIPTION) || result.get(USER_INTENT_DESCRIPTION) == null) {
            result.put(USER_INTENT_DESCRIPTION, "查询订单信息");
        }
    }

    /**
     * 简化版JSON解析逻辑
     */
    private Map<String, Object> parseExtractionResultSimple(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 提取 queryType
            String queryType = extractJsonField(text, QUERY_TYPE);
            if (StrUtil.isNotBlank(queryType) && !"null".equalsIgnoreCase(queryType)) {
                result.put(QUERY_TYPE, queryType);
            }

            // 2. 提取 orderNumber
            String orderNumber = extractJsonField(text, ORDER_NUMBER);
            if (StrUtil.isNotBlank(orderNumber) && !"null".equalsIgnoreCase(orderNumber)) {
                result.put(ORDER_NUMBER, orderNumber);
            }

            // 3. 提取 orderStatus
            String orderStatus = extractJsonField(text, ORDER_STATUS);
            if (StrUtil.isNotBlank(orderStatus) && !"null".equalsIgnoreCase(orderStatus)) {
                result.put(ORDER_STATUS, orderStatus);
            }

            // 4. 提取 productInfo
            String productInfo = extractJsonField(text, PRODUCT_INFO);
            if (StrUtil.isNotBlank(productInfo) && !"null".equalsIgnoreCase(productInfo)) {
                result.put(PRODUCT_INFO, productInfo);
            }

            // 5. 提取 timeRange
            String timeRangeStr = extractTimeRangeField(text);
            if (StrUtil.isNotBlank(timeRangeStr) && !"null".equalsIgnoreCase(timeRangeStr)) {
                result.put(TIME_RANGE, parseTimeRangeFromString(timeRangeStr));
            }

            // 6. 提取 extractedKeywords
            String keywordsStr = extractJsonArrayField(text, EXTRACTED_KEYWORDS);
            if (StrUtil.isNotBlank(keywordsStr) && !"null".equalsIgnoreCase(keywordsStr)) {
                result.put(EXTRACTED_KEYWORDS, parseStringArray(keywordsStr));
            }

            // 7. 提取 userIntentDescription
            String intentDesc = extractJsonField(text, USER_INTENT_DESCRIPTION);
            if (StrUtil.isNotBlank(intentDesc) && !"null".equalsIgnoreCase(intentDesc)) {
                result.put(USER_INTENT_DESCRIPTION, intentDesc);
            }
        } catch (Exception e) {
            log.warn("【订单查询节点】简化解析时出错: {}", e.getMessage());
        }

        log.debug("【订单查询节点】简化解析结果: {}", result);
        return result;
    }

    /**
     * 提取JSON字段值
     */
    private String extractJsonField(String text, String key) {
        if (StrUtil.isBlank(text) || StrUtil.isBlank(key)) {
            return null;
        }

        try {
            // 构建匹配该字段值的正则表达式
            Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }

            // 尝试匹配非字符串值（如 null、true、false、数字）
            Pattern nonStringPattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(null|true|false|\\d+)");
            Matcher nonStringMatcher = nonStringPattern.matcher(text);
            if (nonStringMatcher.find()) {
                return nonStringMatcher.group(1);
            }

            return null;
        } catch (Exception e) {
            log.warn("【订单查询节点】提取字段[{}]时出错: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 提取JSON数组字段
     */
    private String extractJsonArrayField(String text, String key) {
        if (StrUtil.isBlank(text) || StrUtil.isBlank(key)) {
            return null;
        }

        try {
            // 构建匹配数组字段的正则表达式
            Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }

            return null;
        } catch (Exception e) {
            log.warn("【订单查询节点】提取数组字段[{}]时出错: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 解析字符串数组
     */
    private List<String> parseStringArray(String arrayStr) {
        try {
            // 移除引号和空格，然后分割
            String[] items = arrayStr.split(",");
            return List.of(items)
                    .stream()
                    .map(item -> item.trim().replace("\"", ""))
                    .filter(StrUtil::isNotBlank)
                    .toList();
        } catch (Exception e) {
            log.warn("【订单查询节点】解析数组失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 提取时间范围字段
     */
    private String extractTimeRangeField(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }

        try {
            // 查找timeRange对象
            Pattern pattern = Pattern.compile("\"timeRange\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }

            return null;
        } catch (Exception e) {
            log.warn("【订单查询节点】提取timeRange时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析时间范围字符串
     */
    private Map<String, Object> parseTimeRangeFromString(String timeRangeStr) {
        Map<String, Object> timeRange = new HashMap<>();

        try {
            // 提取startTime
            Pattern startPattern = Pattern.compile("\"startTime\"\\s*:\\s*\"([^\"]+)\"");
            Matcher startMatcher = startPattern.matcher(timeRangeStr);
            if (startMatcher.find()) {
                timeRange.put(START_TIME, startMatcher.group(1));
            }

            // 提取endTime
            Pattern endPattern = Pattern.compile("\"endTime\"\\s*:\\s*\"([^\"]+)\"");
            Matcher endMatcher = endPattern.matcher(timeRangeStr);
            if (endMatcher.find()) {
                timeRange.put(END_TIME, endMatcher.group(1));
            }
        } catch (Exception e) {
            log.warn("【订单查询节点】解析timeRange失败: {}", e.getMessage());
        }

        return timeRange;
    }

    /**
     * 获取默认的提取结果
     */
    private Map<String, Object> getDefaultExtractionResult(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put(QUERY_TYPE, "BY_USER_RECENT");
        result.put(ORDER_NUMBER, null);
        result.put(ORDER_STATUS, null);
        result.put(PRODUCT_INFO, null);

        // 默认时间范围：最近30天
        Map<String, Object> timeRange = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        timeRange.put(START_TIME, now.minusDays(30).format(NORM_DATETIME_FORMATTER));
        timeRange.put(END_TIME, now.format(NORM_DATETIME_FORMATTER));
        result.put(TIME_RANGE, timeRange);

        result.put(EXTRACTED_KEYWORDS, List.of());
        result.put(USER_INTENT_DESCRIPTION, "查询用户订单信息");
        result.put(USER_ID, userId);

        return result;
    }

    /**
     * 清理 markdown 代码块标记，移除 ```json 和 ``` 等标记，保留纯JSON内容
     */
    private String cleanMarkdownCodeBlock(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }

        // 移除 ```json 或 ``` 开头（不区分大小写）、移除 ``` 结尾
        String result = text.replaceAll("^(?i)```(?:json)?\\s*", "")
                .replaceAll("```\\s*$", "")
                .trim();

        log.debug("【订单查询节点】清理markdown后内容: {}", result);
        return result;
    }

}