package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.function.WeatherFunction;
import cn.example.ai.demo.service.AiToolCallService;
import cn.example.ai.demo.tools.DateTimeTools;
import cn.example.ai.demo.tools.WeatherV2Tools;
import cn.hutool.core.util.ReflectUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;

/**
 * @author 11030
 */
@Service
public class AiToolCallServiceImpl implements AiToolCallService {

    @Resource(name = "qwenChatClient")
    private ChatClient qwenChatClient;

    @Override
    public String getCurrentTime(String msg) {
        return qwenChatClient.prompt().user(msg).tools(new DateTimeTools()).call().content();
    }

    @Override
    public String getCityWeather(String msg) {
        // 获取方法
        Method method = ReflectUtil.getMethod(WeatherV2Tools.class, "getCityWeatherV2", String.class);

        // 获取方法输入参数schema
        String inputSchema = JsonSchemaGenerator.generateForMethodInput(method);

        // 创建MethodToolCallback
        ToolCallback methodToolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .name("getCityWeatherV2")  //必填
                        .inputSchema(inputSchema)  //必填
                        .build())
                .toolMethod(method)
                .toolObject(new WeatherV2Tools())
                .build();

        // 创建FunctionToolCallback
        ToolCallback functionToolCallback = FunctionToolCallback.builder("getCityWeatherV3", new WeatherFunction())
                .description("输入城市获取天气信息")
                .inputType(String.class)
                .build();

        return qwenChatClient.prompt().user(msg).toolCallbacks(functionToolCallback, methodToolCallback).call().content();
    }

}
