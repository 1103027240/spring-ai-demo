package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.OrderService;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单查询节点
 * @author 11030
 */
@Slf4j
@Component
public class OrderQueryNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("开始订单查询节点");
        String userInput = state.value("userInput", String.class)
                .orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");
        OrderService orderService = SpringUtil.getBean("orderService");

        // 构建提取订单信息提示词
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

        // 1、调用大模型进行提取订单信息（JSON字符串）
        String extractionResult = qwenChatClient.prompt()
                .user(extractPrompt)
                .call()
                .content()
                .trim();
        log.info("订单信息提取结果: {}", extractionResult);

        // 解析提取订单结果（解析JSON字符串）
        Map<String, Object> orderInfo = parseExtractionResult(extractionResult);

        // 2、查询订单信息
        List<Map<String, Object>> orders = orderService.queryOrders(orderInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("orderExtraction", orderInfo);
        result.put("orderResults", orders);
        result.put("orderQueryTime", System.currentTimeMillis());

        return result;
    }

    private Map<String, Object> parseExtractionResult(String extractionResult) {
        Map<String, Object> result = new HashMap<>();
        // 简化处理，实际应该解析JSON
        if (extractionResult.contains("orderNumber")) {
            result.put("orderNumber", extractValue(extractionResult, "orderNumber"));
        }
        if (extractionResult.contains("userInfo")) {
            result.put("userInfo", extractValue(extractionResult, "userInfo"));
        }
        if (extractionResult.contains("queryType")) {
            result.put("queryType", extractValue(extractionResult, "queryType"));
        }
        return result;
    }

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