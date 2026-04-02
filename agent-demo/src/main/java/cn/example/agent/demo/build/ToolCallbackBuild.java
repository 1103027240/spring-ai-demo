package cn.example.agent.demo.build;

import cn.example.agent.demo.function.SqlExecuteFunction;
import cn.example.agent.demo.function.WeatherFunction;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ToolCallbackBuild {

    public ToolCallback getWeatherTool() {
        return FunctionToolCallback.builder("getWeatherV3", new WeatherFunction())
                .description("获取指定城市的天气信息。当用户询问天气、气温、下雨、晴天等天气相关问题时调用此工具。")
                .inputType(Map.class)
                .build();
    }

    public ToolCallback getSimpleSqlTool() {
        return FunctionToolCallback.builder("executeSimpleSql", new SqlExecuteFunction())
                .description("执行SQL查询语句。当用户需要查询数据库数据时调用此工具。")
                .inputType(Map.class)
                .build();
    }


}
