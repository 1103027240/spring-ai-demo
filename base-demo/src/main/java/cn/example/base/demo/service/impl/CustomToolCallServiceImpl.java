package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.CustomToolCallService;
import cn.example.base.demo.tools.DateTimeTools;
import cn.example.base.demo.tools.WeatherV2Tools;
import cn.hutool.core.util.ReflectUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;

/**
 * @author 11030
 */
@Service
public class CustomToolCallServiceImpl implements CustomToolCallService {

    @Resource(name = "qwenChatClient")
    private ChatClient qwenChatClient;

    @Override
    public String getCurrentTime(String msg) {
        return qwenChatClient.prompt().user(msg).tools(new DateTimeTools()).call().content();
    }

    @Override
    public String getCityWeather(String msg) {
        // 获取方法
        Method method = ReflectUtil.getMethod(WeatherV2Tools.class, "getCityWeather", String.class);

        // 获取方法输入参数schema
        String inputSchema = JsonSchemaGenerator.generateForMethodInput(method);

        MethodToolCallback methodToolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .name("getCityWeather")  //必填
                        .inputSchema(inputSchema)  //必填
                        .build())
                .toolMethod(method)
                .toolObject(new WeatherV2Tools())
                .build();

        return qwenChatClient.prompt().user(msg).toolCallbacks(methodToolCallback).call().content();
    }

}
