package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.function.DemoToolCallbackProvider;
import cn.example.agent.demo.function.SqlExecuteFunction;
import cn.example.agent.demo.function.WeatherFunction;
import cn.example.agent.demo.service.AgentToolCallService;
import cn.example.agent.demo.tools.CalculatorTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentToolCallServiceImpl implements AgentToolCallService {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Override
    public String doChat(String message) {
        Map<String, Object> map = Map.of("message", message);

        // 创建方式一：FunctionToolCallback
        ToolCallback weatherTool = FunctionToolCallback.builder("getCityWeatherV3", new WeatherFunction())
                .description("输入城市获取天气信息")
                .inputType(String.class)
                .build();

        // 创建方式二：ToolCallbackProvider
        ToolCallback calculatorTool = FunctionToolCallback.builder("executeDemoSql", new SqlExecuteFunction())
                .description("输入参数进行计算，返回结果")
                .inputType(String.class)
                .build();
        ToolCallbackProvider toolProvider = new DemoToolCallbackProvider(List.of(calculatorTool));

        // 创建方式三：@Tool
        CalculatorTools calculatorTools = new CalculatorTools();

        ReactAgent agent = ReactAgent.builder()
                .name("工具调用智能体")
                .model(qwenChatModel)
                .tools(weatherTool, calculatorTool)
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

}
