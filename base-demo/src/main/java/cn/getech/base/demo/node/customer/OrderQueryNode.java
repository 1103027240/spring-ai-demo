package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.converter.OrderStatusConverter;
import cn.getech.base.demo.enums.OrderStatusEnum;
import cn.getech.base.demo.service.OrderService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.getech.base.demo.constant.PatternConstant.*;
import static cn.getech.base.demo.enums.OrderQueryTypeEnum.BY_USER_RECENT;
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

        // 2. 解析订单提取结果
        Map<String, Object> extractionInfo = parseExtractionResult(extractionResult, userId);

        // 3. 构建查询参数
        Map<String, Object> queryParams = buildQueryParams(extractionInfo);

        // 4. 查询订单信息
        List<Map<String, Object>> orders = orderService.queryOrders(queryParams);

        // 5. 处理查询结果
        Map<String, Object> result = processQueryResult(orders, extractionInfo);

        log.info("【订单查询节点】执行成功，查询类型: {}, 结果数量: {}", extractionInfo.get(QUERY_TYPE), orders.size());
        return result;
    }

    /**
     * 使用大模型提取订单信息
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
     * 解析订单提取结果
     */
    private Map<String, Object> parseExtractionResult(String aiResponse, Long userId) {
        // 解析JSON响应
        Map<String, Object> extractedInfo = parseAIResponse(aiResponse);

        // 处理用户ID
        if (userId != null) {
            extractedInfo.put(USER_ID, userId);
        }

        // 处理queryType
        String queryType = (String) extractedInfo.get(QUERY_TYPE);
        if (StrUtil.isBlank(queryType)) {
            extractedInfo.put(QUERY_TYPE, BY_USER_RECENT.name());
        }

        // 处理状态映射
        processOrderStatus(extractedInfo);

        // 处理时间范围
        processTimeRange(extractedInfo);

        return extractedInfo;
    }

    /**
     * 解析大模型返回JSON
     */
    private Map<String, Object> parseAIResponse(String aiResponse) {
        Map<String, Object> result;
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

        try {
            String cleanedResponse = cleanMarkdownCodeBlock(aiResponse);
            result = objectMapper.readValue(cleanedResponse, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("【订单查询节点】JSON格式解析失败，尝试简化解析: {}", e.getMessage());
            result = parseAIResponseSimple(aiResponse);
        } catch (Exception e) {
            log.error("【订单查询节点】JSON解析发生未知错误: {}", e.getMessage(), e);
            result = new HashMap<>();
        }

        return result;
    }

    /**
     * 简化解析大模型响应
     */
    private Map<String, Object> parseAIResponseSimple(String aiResponse) {
        Map<String, Object> result = new HashMap<>();
        try {
            extractAndPutField(result, aiResponse, QUERY_TYPE_PATTERN, QUERY_TYPE);
            extractAndPutField(result, aiResponse, QUERY_ORDER_NUMBER_PATTERN, ORDER_NUMBER);
            extractAndPutField(result, aiResponse, ORDER_STATUS_PATTERN, ORDER_STATUS);
            extractAndPutField(result, aiResponse, PRODUCT_INFO_PATTERN, PRODUCT_INFO);
        } catch (Exception e) {
            log.error("【订单提取】简化解析失败", e);
        }
        return result;
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(Map<String, Object> extractedInfo) {
        Map<String, Object> queryParams = new HashMap<>();

        // 用户ID
        queryParams.put(USER_ID, extractedInfo.get(USER_ID));

        // 查询类型
        if (extractedInfo.containsKey(QUERY_TYPE)) {
            queryParams.put(QUERY_TYPE, extractedInfo.get(QUERY_TYPE));
        }

        // 订单号
        putIfValid(queryParams, extractedInfo, ORDER_NUMBER);

        // 订单状态
        putIfValid(queryParams, extractedInfo, ORDER_STATUS);

        // 商品信息
        if (extractedInfo.containsKey(PRODUCT_INFO) && extractedInfo.get(PRODUCT_INFO) != null) {
            queryParams.put(PRODUCT_KEYWORD, extractedInfo.get(PRODUCT_INFO));
        }

        // 时间范围
        processTimeRangeParams(queryParams, extractedInfo);

        // 设置默认排序
        queryParams.put(ORDER_BY, CREATE_TIME);
        queryParams.put(ORDER_DESC, DEFAULT_ORDER_DESC);

        // 设置默认分页
        queryParams.put(LIMIT, DEFAULT_LIMIT);

        log.debug("【订单查询节点】构建查询参数: {}", queryParams);
        return queryParams;
    }

    /**
     * 使用正则表达式提取字段并放入Map
     */
    private void extractAndPutField(Map<String, Object> result, String aiResponse, Pattern pattern, String key) {
        Matcher matcher = pattern.matcher(aiResponse);
        if (matcher.find()) {
            String value = matcher.group(1);
            if (StrUtil.isNotBlank(value) && !"null".equalsIgnoreCase(value)) {
                result.put(key, value);
            }
        }
    }

    /**
     * 将有效值放入Map
     */
    private void putIfValid(Map<String, Object> target, Map<String, Object> source, String key) {
        if (source.containsKey(key) && source.get(key) != null) {
            target.put(key, source.get(key));
        }
    }

    /**
     * 处理时间范围参数
     */
    private void processTimeRangeParams(Map<String, Object> queryParams, Map<String, Object> extractedInfo) {
        Object startTime = extractedInfo.get(START_TIME);
        Object endTime = extractedInfo.get(END_TIME);

        if (startTime != null) {
            queryParams.put(START_TIME, startTime);
        }
        if (endTime != null) {
            queryParams.put(END_TIME, endTime);
        }
    }

    /**
     * 处理时间范围
     */
    private void processTimeRange(Map<String, Object> orderInfo) {
        if (orderInfo.containsKey(TIME_RANGE) && orderInfo.get(TIME_RANGE) instanceof Map) {
            try {
                Map<String, Object> timeRange = (Map<String, Object>) orderInfo.get(TIME_RANGE);
                parseAndPutTimeField(timeRange, START_TIME, orderInfo);
                parseAndPutTimeField(timeRange, END_TIME, orderInfo);
            } catch (DateTimeParseException e) {
                log.warn("【订单查询节点】时间格式解析失败: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("【订单查询节点】处理时间范围失败: {}", e.getMessage());
            }
        }
        setDefaultTimeRange(orderInfo);
    }

    /**
     * 解析并放入时间字段
     */
    private void parseAndPutTimeField(Map<String, Object> timeRange, String fieldName, Map<String, Object> orderInfo) {
        if (timeRange.containsKey(fieldName) && timeRange.get(fieldName) instanceof String) {
            String timeStr = (String) timeRange.get(fieldName);
            if (StrUtil.isNotBlank(timeStr) && !"null".equalsIgnoreCase(timeStr)) {
                orderInfo.put(fieldName, LocalDateTime.parse(timeStr, NORM_DATETIME_FORMATTER));
            }
        }
    }

    /**
     * 设置默认时间范围
     */
    private void setDefaultTimeRange(Map<String, Object> orderInfo) {
        LocalDateTime now = LocalDateTime.now();
        if (!orderInfo.containsKey(START_TIME) || orderInfo.get(START_TIME) == null) {
            orderInfo.put(START_TIME, now.minusDays(DEFAULT_DAYS_RANGE));
        }
        if (!orderInfo.containsKey(END_TIME) || orderInfo.get(END_TIME) == null) {
            orderInfo.put(END_TIME, now);
        }
    }

    /**
     * 处理订单状态映射
     */
    private void processOrderStatus(Map<String, Object> extractedInfo) {
        if (extractedInfo.containsKey(ORDER_STATUS) && extractedInfo.get(ORDER_STATUS) != null) {
            String statusStr = extractedInfo.get(ORDER_STATUS).toString();
            Integer status = OrderStatusConverter.convertToStatusCode(statusStr);
            if (status != null) {
                extractedInfo.put(ORDER_STATUS, status);
            } else {
                extractedInfo.put(ORDER_STATUS, null);
            }
        }
    }

    /**
     * 处理查询结果
     */
    private Map<String, Object> processQueryResult(List<Map<String, Object>> orders, Map<String, Object> extractionInfo) {
        Map<String, Object> result = new HashMap<>();

        result.put(ORDER_RESULTS, orders);
        result.put(ORDER_EXTRACTION, extractionInfo);
        result.put(QUERY_TYPE, extractionInfo.get(QUERY_TYPE));
        result.put(ORDER_QUERY_TIME, System.currentTimeMillis());

        // 生成摘要
        String summary = generateOrderSummary(orders);
        result.put(QUERY_SUMMARY, summary);

        return result;
    }

    /**
     * 生成订单摘要
     */
    private String generateOrderSummary(List<Map<String, Object>> orders) {
        if (CollUtil.isEmpty(orders)) {
            return NO_ORDERS_FOUND_MESSAGE;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format(ORDERS_FOUND_TEMPLATE, orders.size()));

        int displayCount = Math.min(orders.size(), SUMMARY_DISPLAY_COUNT);
        for (int i = 0; i < displayCount; i++) {
            Map<String, Object> order = orders.get(i);
            appendOrderItem(summary, i + 1, order);
        }

        if (orders.size() > SUMMARY_DISPLAY_COUNT) {
            summary.append(String.format(MORE_ORDERS_TEMPLATE, orders.size() - SUMMARY_DISPLAY_COUNT));
        }

        return summary.toString();
    }

    /**
     * 追加订单项到摘要
     */
    private void appendOrderItem(StringBuilder summary, int index, Map<String, Object> order) {
        String orderNumber = (String) order.get(ORDER_NUMBER);
        String statusDesc = getStatusDescription(order.get(STATUS));
        summary.append(String.format(ORDER_ITEM_TEMPLATE, index, orderNumber, statusDesc));
    }

    /**
     * 获取订单状态描述
     */
    private String getStatusDescription(Object statusObj) {
        if (statusObj instanceof Integer status) {
            return OrderStatusEnum.getDescription(status);
        } else if (statusObj != null) {
            return statusObj.toString();
        }
        return "未知状态";
    }

    /**
     * 清理markdown代码块，移除 ```json 和 ``` 等标记，保留纯JSON内容
     */
    private String cleanMarkdownCodeBlock(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        String result = MARKDOWN_CODE_BLOCK_PATTERN.matcher(text).replaceAll("").trim();
        log.debug("【订单查询节点】清理markdown后内容: {}", result);
        return result;
    }

}