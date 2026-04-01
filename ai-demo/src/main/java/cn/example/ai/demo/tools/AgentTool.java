package cn.example.ai.demo.tools;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.function.FunctionToolCallback;
import java.util.Map;
import java.util.function.Function;

/**
 * 智能体工具包装类
 * 将 ReactAgent 包装成 FunctionToolCallback，使其可以作为工具被其他智能体调用
 */
public class AgentTool {

    /**
     * 将 ReactAgent 包装成 FunctionToolCallback
     *
     * @param agent          要包装的智能体
     * @param toolName       工具名称
     * @param toolDescription 工具描述
     * @return FunctionToolCallback 工具回调
     */
    public static FunctionToolCallback<String, String> getFunctionToolCallback(
            ReactAgent agent,
            String toolName,
            String toolDescription) {

        return FunctionToolCallback.<String, String>builder()
                .name(toolName)
                .description(toolDescription)
                .inputType(String.class)
                .outputType(String.class)
                .function(createAgentFunction(agent))
                .build();
    }

    /**
     * 将 ReactAgent 包装成 FunctionToolCallback（支持Map输入）
     *
     * @param agent          要包装的智能体
     * @param toolName       工具名称
     * @param toolDescription 工具描述
     * @return FunctionToolCallback 工具回调
     */
    public static FunctionToolCallback<Map<String, Object>, String> getFunctionToolCallbackWithMapInput(
            ReactAgent agent,
            String toolName,
            String toolDescription) {

        return FunctionToolCallback.<Map<String, Object>, String>builder()
                .name(toolName)
                .description(toolDescription)
                .inputType(Map.class)
                .outputType(String.class)
                .function(createAgentFunctionWithMap(agent))
                .build();
    }

    /**
     * 创建智能体执行函数（String输入）
     */
    private static Function<String, String> createAgentFunction(ReactAgent agent) {
        return (String input) -> {
            try {
                AssistantMessage result = agent.call(input);
                return result.getText();
            } catch (Exception e) {
                return "智能体执行失败: " + e.getMessage();
            }
        };
    }

    /**
     * 创建智能体执行函数（Map输入）
     */
    private static Function<Map<String, Object>, String> createAgentFunctionWithMap(ReactAgent agent) {
        return (Map<String, Object> input) -> {
            try {
                // 将Map转换为字符串作为输入
                String message = input.values().stream()
                        .map(Object::toString)
                        .reduce((a, b) -> a + " " + b)
                        .orElse("");
                AssistantMessage result = agent.call(message);
                return result.getText();
            } catch (Exception e) {
                return "智能体执行失败: " + e.getMessage();
            }
        };
    }

}
