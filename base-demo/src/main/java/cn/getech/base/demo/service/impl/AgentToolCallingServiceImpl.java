package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.AgentToolCallingService;
import cn.getech.base.demo.tools.WeatherTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class AgentToolCallingServiceImpl implements AgentToolCallingService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Override
    public String doChat(String message) {
        Toolkit toolkit = new Toolkit();

        // 创建工具组
        toolkit.createToolGroup("basic", "基础工具", true);  //激活

        // 注册工具组
        toolkit.registration()
                .tool(new WeatherTools())
                .group("basic")
                .apply();

        // 更新工具组
        // toolkit.updateToolGroups(List.of("basic"), false);  //停用

        // 工具调用超时和重试配置
        ExecutionConfig toolExecutionConfig = ExecutionConfig.builder()
                .timeout(Duration.ofSeconds(30))
                .maxAttempts(3)
                .build();

        // 调用大模型
        ReActAgent agent = ReActAgent.builder()
                .name("agentTool")
                .sysPrompt("你是一个AI助手。你必须始终使用中文回复所有问题，无论输入是什么语言。不要使用英文。")
                .model(qwenAgentChatModel)
                .toolkit(toolkit)
                .toolExecutionConfig(toolExecutionConfig)
                .build();

        // 非阻塞调用
        Mono<Msg> responseMono = agent.call(
                Msg.builder()
                        .textContent(message)
                        .build()
        );

        // 阻塞处理结果
        return responseMono.block().getTextContent();
    }

}
