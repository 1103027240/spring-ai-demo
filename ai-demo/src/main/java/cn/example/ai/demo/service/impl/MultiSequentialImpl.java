package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.build.StudioBuild;
import cn.example.ai.demo.service.MultiSequentialService;
import cn.example.ai.demo.service.ReturnProcessSequentialService;
import cn.hutool.core.util.StrUtil;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import io.agentscope.core.studio.StudioManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MultiSequentialImpl implements MultiSequentialService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Autowired
    private ReturnProcessSequentialService returnProcessSequentialService;

    @Autowired
    private StudioBuild studioBuild;

    @Override
    public Map<String, Object> doChat(String message) {
        String orderId = "";

        try {
            studioBuild.initStudio("顺序多智能体");

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
        } finally {
            StudioManager.shutdown();
        }
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
                .name("退货流程处理智能体")
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

}
