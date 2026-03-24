package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.MultiSequentialService;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class MultiSequentialImpl implements MultiSequentialService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Resource(name = "returnProcessSequentialAgent")
    private SequentialAgent returnProcessSequentialAgent;

    @Override
    public Map<String, String> doChat(String message) {
        // 1、调用大模型提取订单号
        String orderId = extractOrderId(message);
        if(StrUtil.isBlank(orderId)){
            return Map.of("orderId", orderId, "status", "失败", "msg", "用户输入不包含订单号");
        }

        // 2、调用顺序退货流程处理智能体
        OverAllState overAllState;
        try {
            overAllState = returnProcessSequentialAgent.invoke(orderId).orElse(null);
        } catch (GraphRunnerException e) {
            return Map.of("orderId", orderId, "status", "错误", "msg", e.getMessage());
        }

        // 3、封装返回结果
        if (overAllState == null) {
            return Map.of("orderId", orderId, "status", "失败", "msg", "代理未返回结果");
        }

        return Map.of(
                "orderId", orderId,
                "status", "成功",
                "orderCheckResult", extractText(overAllState, "orderCheckResult"),
                "policyCheckResult", extractText(overAllState, "policyCheckResult"),
                "refundAmount", extractText(overAllState, "refundAmount"),
                "returnOrder", extractText(overAllState, "returnOrder"));
    }

    private String extractOrderId(String message) {
        String extractPrompt = """
            请从用户输入中提取订单ID：
            1. 如果能提取到订单ID，格式为"{订单ID}"，例如"1234567890"        
            2. 如果不能提取到订单ID，返回""，格式为""
            
            只返回提取结果，不要包含任何其他内容等。
            
            用户输入：%s
                """.formatted(message);

        ReActAgent agent = ReActAgent.builder()
                .name("MultiSequentialAgent")
                .model(qwenAgentChatModel)
                .build();

        Msg msg = Msg.builder()
                .content(List.of(TextBlock.builder()
                        .text(extractPrompt)
                        .build()))
                .build();

        return agent.call(msg)
                .block()
                .getTextContent();
    }

    private String extractText(OverAllState overAllState, String key) {
        Object value = overAllState.value(key).or(null);
        if (value == null) {
            return null;
        }

        if (value instanceof Message message) {
            return message.getText();
        }
        return value != null ? value.toString() : null;
    }

}
