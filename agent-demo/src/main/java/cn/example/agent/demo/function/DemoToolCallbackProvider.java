package cn.example.agent.demo.function;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import java.util.List;

public class DemoToolCallbackProvider implements ToolCallbackProvider {
    private List<ToolCallback> toolCallbacks;

    public DemoToolCallbackProvider (List<ToolCallback> toolCallbacks) {
        this.toolCallbacks = toolCallbacks;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return toolCallbacks.toArray(new ToolCallback[0]);
    }

}
