package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.OrderService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static cn.getech.base.demo.constant.FieldValueConstant.*;

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

        // 3. 查询订单信息
        List<Map<String, Object>> orders = orderService.queryOrders(orderInfo);
        log.info("【订单查询节点】查询完成，找到[{}]条数据", orders.size());

        Map<String, Object> result = new HashMap<>();
        result.put(ORDER_EXTRACTION, orderInfo);
        result.put(ORDER_RESULTS, orders);
        result.put(ORDER_QUERY_TIME, System.currentTimeMillis());
        return result;
    }

    /**
     * 从用户输入中提取订单信息
     */
    private String extractOrderInfo(String userInput) {
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        String extractPrompt = """
                请从以下用户输入中提取订单相关信息：
                1. 订单号（如果有）
                2. 用户可能的信息（用户名、手机号等）
                3. 查询的具体内容（订单状态、物流信息等）
                
                用户输入：%s
                
                请以JSON格式返回提取结果，格式如下：
                {
                    "orderNumber": "提取到的订单号或null",
                    "userInfo": "提取到的用户信息或null",
                    "queryType": "orderStatus/logistics/other"
                }
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
            result.put(USER_ID, userId);
            result.put(QUERY_TYPE, "other");
            return result;
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

        // 确保queryType有默认值
        if (!result.containsKey(QUERY_TYPE) || result.get(QUERY_TYPE) == null) {
            result.put(QUERY_TYPE, "other");
        }

        log.info("【订单查询节点】解析订单提取结果: {}", result);
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

    /**
     * 简化版JSON解析逻辑
     */
    private Map<String, Object> parseExtractionResultSimple(String text) {
        Map<String, Object> result = new HashMap<>();

        // 1. 从 orderNumber 字段提取
        String orderNumber = extractValue(text, ORDER_NUMBER);
        if (StrUtil.isNotBlank(orderNumber) && !"null".equalsIgnoreCase(orderNumber)) {
            result.put(ORDER_NUMBER, orderNumber);
        }

        // 2. 从 userInfo 字段提取
        String userInfo = extractValue(text, USER_INFO);
        if (StrUtil.isNotBlank(userInfo) && !"null".equalsIgnoreCase(userInfo)) {
            result.put(USER_INFO, userInfo);
        }

        // 3. 从 queryType 字段提取
        String queryType = extractValue(text, QUERY_TYPE);
        if (StrUtil.isNotBlank(queryType) && !"null".equalsIgnoreCase(queryType)) {
            result.put(QUERY_TYPE, queryType);
        } else {
            result.put(QUERY_TYPE, "other");
        }

        log.debug("【订单查询节点】简化解析结果: {}", result);
        return result;
    }

    /**
     * 从文本中提取指定字段的值
     */
    private String extractValue(String text, String key) {
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

}