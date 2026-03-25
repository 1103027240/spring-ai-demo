package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.MultiSequentialService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import io.agentscope.core.studio.StudioManager;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

@Service
public class MultiSequentialImpl implements MultiSequentialService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Resource(name = "returnProcessSequentialAgent")
    private SequentialAgent returnProcessSequentialAgent;

    @Override
    public Map<String, Object> doChat(String message) {
        // 初始化Studio连接
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("MultiSequentialAgent")
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();

        // 1、调用大模型提取订单号
        String orderId = extractOrderId(message);
        if(StrUtil.isBlank(orderId)){
            StudioManager.shutdown();
            return Map.of("status", "fail", "orderId", orderId, "msg", "用户输入不包含订单号");
        }

        // 2、调用顺序退货流程处理智能体
        OverAllState overAllState;
        try {
            overAllState = returnProcessSequentialAgent.invoke(initOrderMap(orderId)).orElse(null);
        } catch (GraphRunnerException e) {
            StudioManager.shutdown();
            return Map.of("status", "error", "orderId", orderId,  "msg", e.getMessage());
        }

        // 3、封装返回结果
        if (overAllState == null || CollUtil.isEmpty(overAllState.data())) {
            StudioManager.shutdown();
            return Map.of("status", "fail", "orderId", orderId, "msg", "代理未返回结果");
        }

        Map<String, Object> dataMap = overAllState.data();
        StudioManager.shutdown();
        return Map.of(
                "status", "success",
                "orderId", orderId,
                "orderCheckResult", extractText(dataMap, "orderCheckResult"),
                "policyCheckResult", extractText(dataMap, "policyCheckResult"),
                "refundAmount", extractText(dataMap, "refundAmount"),
                "returnOrder", extractText(dataMap, "returnOrder"));
    }

    private String extractOrderId(String message) {
        String extractPrompt = """
            请从用户输入中提取订单号：
            1. 如果能提取到订单号，直接返回订单号       
            2. 如果不能提取到订单号，直接返回""
            
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

        String result = agent.call(msg)
                .block()
                .getTextContent();

        return StrUtil.unWrap(result, '"', '"');  //例""""，去掉前后""，只返回""
    }

    private String extractText(Map<String, Object> dataMap, String key) {
        Object value = dataMap.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Message message) {
            return message.getText();
        }
        return value != null ? value.toString() : null;
    }

    private Map<String, Object> initOrderMap(String orderId) {
        return Map.of(
                "orderId", orderId,
                "status", "Shipped",
                "amount", new SecureRandom().nextInt(1000) + 1,
                "createTime", DateUtil.format(LocalDateTime.now().plusDays(new SecureRandom().nextLong(16)), NORM_DATETIME_PATTERN));
    }

}
