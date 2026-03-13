package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.OrderService;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
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

        // 1.调用大模型提取订单信息
        String extractionResult = extractOrderInfo(userInput);

        // 2、解析提取订单结果（解析JSON字符串）
        Map<String, Object> orderInfo = parseExtractionResult(extractionResult, userId);

        // 3.查询订单信息
        List<Map<String, Object>> orders = orderService.queryOrders(orderInfo);
        log.info("【订单查询节点】查询完成，找到[{}]条数据", orders.size());

        Map<String, Object> result = new HashMap<>();
        result.put(ORDER_EXTRACTION, orderInfo);
        result.put(ORDER_RESULTS, orders);
        result.put(ORDER_QUERY_TIME, System.currentTimeMillis());
        return result;
    }

    private String extractOrderInfo(String userInput){
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
     * 解析提取结果
     */
    private Map<String, Object> parseExtractionResult(String extractionResult, Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 移除 markdown 代码块标记
        String cleanedResult = cleanMarkdownCodeBlock(extractionResult);
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        try {
            Map<String, Object> parsedMap = objectMapper.readValue(cleanedResult, new TypeReference<>() {});
            result.putAll(parsedMap);
        } catch (Exception jsonException) {
            log.error("【订单查询节点】Json解析失败，使用简化提取: {}", cleanedResult, jsonException);
            result = parseExtractionResultSimple(cleanedResult);
        }

        result.put(USER_ID, userId);
        log.info("【订单查询节点】解析订单提前结果: {}", result);
        return result;
    }

    /**
     * 清理 markdown 代码块标记
     */
    private String cleanMarkdownCodeBlock(String text) {
        if (text == null) {
            return null;
        }
        // 移除 ```json 或 ``` 开头
        String cleaned = text.replaceAll("^```(?:json|JSON)?\\s*", "");
        // 移除 ``` 结尾
        cleaned = cleaned.replaceAll("```\\s*$", "");
        return cleaned.trim();
    }

    /**
     * 简化提取逻辑
     */
    private Map<String, Object> parseExtractionResultSimple(String text) {
        Map<String, Object> result = new HashMap<>();

        // 1. 从orderNumber字段提取
        if (text.contains("\"orderNumber\"")) {
            String orderNumber = extractValue(text, ORDER_NUMBER);
            if (orderNumber != null && !orderNumber.equals("null")) {
                result.put(ORDER_NUMBER, orderNumber);
            }
        }

        // 2. 从userInfo字段提取
        if (text.contains("\"userInfo\"")) {
            String userInfo = extractValue(text, USER_INFO);
            if (userInfo != null && !userInfo.equals("null")) {
                result.put(USER_INFO, userInfo);
            }
        }

        // 3. 从queryType字段提取
        if (text.contains("\"queryType\"")) {
            String queryType = extractValue(text, QUERY_TYPE);
            if (queryType != null && !queryType.equals("null")) {
                result.put(QUERY_TYPE, queryType);
            } else {
                result.put(QUERY_TYPE, "other");
            }
        } else {
            result.put(QUERY_TYPE, "other");
        }

        return result;
    }

    /**
     * 提取值
     */
    private String extractValue(String text, String key) {
        // 简化提取逻辑
        int start = text.indexOf("\"" + key + "\"");
        if (start == -1) {
            return null;
        }

        int colon = text.indexOf(":", start);
        int comma = text.indexOf(",", colon);
        int end = text.indexOf("}", colon);
        if (comma != -1 && comma < end) {
            end = comma;
        }

        String value = text.substring(colon + 1, end).trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }

}