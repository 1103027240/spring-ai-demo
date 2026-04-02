package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.build.ToolCallbackBuild;
import cn.example.agent.demo.function.DemoToolCallbackProvider;
import cn.example.agent.demo.service.AgentToolCallService;
import cn.example.agent.demo.tools.CalculatorTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentToolCallServiceImpl implements AgentToolCallService {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Autowired
    private ToolCallbackBuild toolCallbackBuild;

    @Override
    public String doChat(String message) {
        Map<String, Object> map = Map.of("message", message);

        // 创建方式一：FunctionToolCallback
        ToolCallback weatherTool = toolCallbackBuild.getWeatherTool();

        // 创建方式二：ToolCallbackProvider
        ToolCallback simpleSqlTool = toolCallbackBuild.getSimpleSqlTool();
        ToolCallbackProvider toolProvider = new DemoToolCallbackProvider(List.of(simpleSqlTool));

        // 创建方式三：@Tool注解
        CalculatorTools calculatorTools = new CalculatorTools();

        // 直接调用工具
        invokeToolCallback(calculatorTools);

        // 通过大模型调用工具
        ReactAgent agent = ReactAgent.builder()
                .name("工具调用智能体")
                .model(qwenChatModel)
                .systemPrompt("""
                    你是一个AI助手，可以使用工具来帮助用户解决问题。
                    
                    可用工具列表：
                    - getWeatherV3: 获取城市天气，参数为城市名称字符串
                    - executeSimpleSql: 执行SQL查询
                    - add: 两数相加
                    - subtract: 两数相减
                    
                    重要规则：
                    1. 你必须始终使用中文回复
                    """)
                .instruction("用户问题：{message}")
                .tools(weatherTool, simpleSqlTool)
                .toolCallbackProviders(toolProvider)
                .methodTools(calculatorTools)
                .toolContext(Map.of("userId", "10001")) //工具调用上下文
                .toolExecutionTimeout(Duration.ofSeconds(30)) //工具调用超时时间设置
                .build();
        try {
            return agent.call(map).getText();
        } catch (Exception e) {
            log.error("工具调用智能体执行报错", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 直接调用工具
     */
    public void invokeToolCallback(CalculatorTools calculatorTools) {
        // 通过工具直接调用
        String result = calculatorTools.add(1, 2);
        AssistantMessage assistantMessage2 = AssistantMessage.builder().content(result).build();
        log.info("assistantMessage2: {}", assistantMessage2.getText());

        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(List.of(new ToolResponseMessage.ToolResponse(UUID.randomUUID().toString(), "add", result)))
                .build();

        String toolResult = toolResponseMessage.getResponses().stream()
                .map(ToolResponseMessage.ToolResponse::responseData)
                .collect(Collectors.joining(","));

        log.info("toolResponseMessage: {}", toolResult);
    }

}
