package cn.example.agent.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class CalculatorTools {

    @Tool(description = "计算两数相加")
    public String add(@ToolParam(description = "第一个参数") int a, @ToolParam(description = "第二个参数") int b) {
        return String.valueOf(a + b);
    }

    @Tool(description = "计算两数相减")
    public String subtract(@ToolParam(description = "第一个参数") int a, @ToolParam(description = "第二个参数") int b) {
        return String.valueOf(a - b);
    }

}
