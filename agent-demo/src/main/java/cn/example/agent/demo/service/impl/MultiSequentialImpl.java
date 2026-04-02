package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.MultiSequentialService;
import cn.example.agent.demo.service.ReturnProcessSequentialService;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class MultiSequentialImpl implements MultiSequentialService {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Autowired
    private ReturnProcessSequentialService returnProcessSequentialService;

    @Override
    public Map<String, Object> doChat(String message) {
        String orderId = "";
        try {
            // 1、调用大模型提取订单号
            orderId = extractOrderId(message);
            if(StrUtil.isBlank(orderId)){
                return Map.of("status", "fail", "orderId", orderId, "msg", "用户输入不包含订单号");
            }

            // 2、调用顺序退货流程处理智能体
            return returnProcessSequentialService.runSequential(orderId);
        } catch (Exception e) {
            log.error("【顺序多智能体】执行失败", e);
            return Map.of("status", "error", "orderId", orderId, "msg", e.getMessage());
        }
    }

    private String extractOrderId(String message) throws GraphRunnerException {
        String extractPrompt = """
            请从用户输入中提取订单号：
            1. 如果能提取到订单号，直接返回订单号
            2. 如果不能提取到订单号，直接返回""
            
            只返回提取结果，不要包含任何其他内容等。
            
            用户输入：{message}
            """;

        ReactAgent agent = ReactAgent.builder()
                .name("退货流程处理智能体")
                .model(qwenChatModel)
                .instruction(extractPrompt)
                .build();

        String result = agent.call(Map.of("message", message)).getText();
        return StrUtil.unWrap(result, '"', '"');  //例""""，去掉前后""，只返回""
    }

}
