package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.function.DemoToolCallbackProvider;
import cn.example.agent.demo.function.SqlExecuteFunction;
import cn.example.agent.demo.function.WeatherFunction;
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
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentToolCallServiceImpl implements AgentToolCallService {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Override
    public String doChat(String message) {
        Map<String, Object> map = Map.of("message", message);

        // 创建方式一：FunctionToolCallback（使用 Map 类型确保 JSON 格式）
        ToolCallback weatherTool = FunctionToolCallback.builder("getWeatherV3", new WeatherFunction())
                .description("获取指定城市的天气信息。当用户询问天气、气温、下雨、晴天等天气相关问题时调用此工具。")
                .inputType(Map.class)
                .build();

        // 创建方式二：ToolCallbackProvider
        ToolCallback sqlTool = FunctionToolCallback.builder("executeSimpleSql", new SqlExecuteFunction())
                .description("执行SQL查询语句。当用户需要查询数据库数据时调用此工具。")
                .inputType(Map.class)
                .build();
        ToolCallbackProvider toolProvider = new DemoToolCallbackProvider(List.of(sqlTool));

        // 创建方式三：@Tool注解
        CalculatorTools calculatorTools = new CalculatorTools();

        // 直接调用工具
        invokeToolCallback(message, calculatorTools);

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
                .tools(weatherTool, sqlTool)
                .toolCallbackProviders(toolProvider)
                .methodTools(calculatorTools)
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
    public void invokeToolCallback(String message, CalculatorTools calculatorTools) {
        String callId = UUID.randomUUID().toString();

        // 方式一、通过ToolCall调用
        AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall(callId, "function", "add", message);
        AssistantMessage assistantMessage1 = AssistantMessage.builder().toolCalls(List.of(toolCall)).build();
        log.info("assistantMessage1: {}", assistantMessage1.getText());

        // 方式二：通过工具直接调用
        String result = calculatorTools.add(1, 2);
        AssistantMessage assistantMessage2 = AssistantMessage.builder().content(result).build();
        log.info("assistantMessage2: {}", assistantMessage2.getText());

        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(List.of(new ToolResponseMessage.ToolResponse(callId, "add", result)))
                .build();

        String toolResult = toolResponseMessage.getResponses().stream()
                .map(ToolResponseMessage.ToolResponse::responseData)
                .collect(Collectors.joining(","));

        log.info("toolResponseMessage: {}", toolResult);
    }

}
