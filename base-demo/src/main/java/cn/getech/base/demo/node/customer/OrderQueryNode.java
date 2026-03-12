package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.OrderService;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

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

        OrderService orderService = SpringUtil.getBean("orderService");
        String userInput = state.value("userInput", String.class).orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));
        Long userId = state.value("userId", Long.class).orElse(null);

        // 1.调用大模型提取订单信息
        String extractionResult = extractOrderInfo(userInput);

        // 解析提取订单结果（解析JSON字符串）
        Map<String, Object> orderInfo = parseExtractionResult(extractionResult);
        orderInfo.put("userId", userId);

        // 2.查询订单信息
        List<Map<String, Object>> orders = orderService.queryOrders(orderInfo);
        log.info("【订单查询节点】查询完成，找到[{}]条数据", orders.size());

        Map<String, Object> result = new HashMap<>();
        result.put("orderExtraction", orderInfo);
        result.put("orderResults", orders);
        result.put("orderQueryTime", System.currentTimeMillis());
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
    private Map<String, Object> parseExtractionResult(String extractionResult) {
        Map<String, Object> result = new HashMap<>();
        ObjectMapper objectMapper = SpringUtil.getBean("objectMapper");
        try {
            // 尝试解析为JSON
            Map<String, Object> parsed = objectMapper.readValue(extractionResult, new TypeReference<>() {});
            result.putAll(parsed);
        } catch (Exception jsonException) {
            log.warn("【订单查询节点】JSON解析失败，使用简化提取: {}", extractionResult, jsonException);
            result = parseExtractionResultSimple(extractionResult);
        }
        return result;
    }

    /**
     * 简化提取逻辑
     */
    private Map<String, Object> parseExtractionResultSimple(String text) {
        Map<String, Object> result = new HashMap<>();

        // 1. 从order_number字段提取
        if (text.contains("\"orderNumber\"")) {
            String orderNumber = extractValue(text, "orderNumber");
            if (orderNumber != null && !orderNumber.equals("null")) {
                result.put("orderNumber", orderNumber);
            }
        }

        // 2. 从user_info字段提取
        if (text.contains("\"userInfo\"")) {
            String userInfo = extractValue(text, "userInfo");
            if (userInfo != null && !userInfo.equals("null")) {
                result.put("userInfo", userInfo);
            }
        }

        // 3. 从query_type字段提取
        if (text.contains("\"queryType\"")) {
            String queryType = extractValue(text, "queryType");
            if (queryType != null && !queryType.equals("null")) {
                result.put("queryType", queryType);
            } else {
                result.put("queryType", "other");
            }
        } else {
            result.put("queryType", "other");
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