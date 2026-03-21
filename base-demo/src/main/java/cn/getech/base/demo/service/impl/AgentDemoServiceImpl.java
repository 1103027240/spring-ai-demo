package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.context.UserContext;
import cn.getech.base.demo.service.AgentDemoService;
import cn.getech.base.demo.tools.SqlTools;
import cn.getech.base.demo.tools.WeatherTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.Toolkit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
@Service
public class AgentDemoServiceImpl implements AgentDemoService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Override
    public String doChat(String message) {
        // 工具调用
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTools());
        toolkit.registerTool(new SqlTools());

        // 工具调用上下文
        ToolExecutionContext toolExecutionContext = ToolExecutionContext.builder()
                .register(new UserContext("10001"))
                .build();

        // 工具调用超时和重试配置
        ExecutionConfig toolExecutionConfig = ExecutionConfig.builder()
                .timeout(Duration.ofSeconds(30))
                .maxAttempts(3)
                .build();

        // 模型调用超时和重试配置
        ExecutionConfig modelExecutionConfig = ExecutionConfig.builder()
                .timeout(Duration.ofSeconds(30))
                .maxAttempts(3)
                .build();

        // 启用PlanNotebook支持多任务
        PlanNotebook planNotebook = PlanNotebook.builder()
                .maxSubtasks(15)
                .build();

        // 调用大模型
        ReActAgent agent = ReActAgent.builder()
                .name("agentDemo")
                .sysPrompt("你是一个AI助手。你必须始终使用中文回复所有问题，无论输入是什么语言。不要使用英文。")
                .model(qwenAgentChatModel)
                .maxIters(10)  // 智能体响应迭代次数，默认10
                .checkRunning(true)  // 阻止并发调用，默认true
                .toolkit(toolkit)
                .toolExecutionContext(toolExecutionContext)
                .modelExecutionConfig(modelExecutionConfig)
                .toolExecutionConfig(toolExecutionConfig)
                .planNotebook(planNotebook)
                .build();

        // 非阻塞调用
        Mono<Msg> responseMono = agent.call(
                Msg.builder()
                        .textContent(message)
                        .build()
        );

        // 阻塞处理结果
        String response = responseMono.block().getTextContent();

        // 或异步处理结果
        //responseMono.subscribe(e -> log.info("response: {}", e.getTextContent()));

        return response;
    }

}
