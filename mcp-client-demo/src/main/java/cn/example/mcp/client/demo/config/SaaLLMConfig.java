package cn.example.mcp.client.demo.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 大语言模型配置
 * @author 11030
 */
@Configuration
public class SaaLLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.qwen.model:qwen-plus}")
    private String qwenModel;

    @Value("${spring.ai.dashscope.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public ChatModel qwenChatModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model(qwenModel)
                        .temperature(0.7d)
                        .topP(0.9d)
                        .maxToken(2048)
                        .build())
                .build();
    }

    @Bean
    public ChatModel deepseekChatModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model(deepseekModel)
                        .temperature(0.7d)
                        .topP(0.9d)
                        .maxToken(2048)
                        .build())
                .build();
    }

    /**
     * 常用qwenChatClient
     */
    @Bean
    public ChatClient qwenChatClient(ChatModel qwenChatModel) {
        return ChatClient.builder(qwenChatModel).build();
    }

    /**
     * 带ToolCall的qwenChatClient
     */
    @Bean(name = "toolQwenChatClient")
    public ChatClient toolQwenChatClient(ChatModel qwenChatModel, ToolCallbackProvider provider) {
        return ChatClient.builder(qwenChatModel)
                .defaultToolCallbacks(provider.getToolCallbacks())
                .build();
    }

    @Bean
    public ChatClient deepseekChatClient(ChatModel deepseekChatModel) {
        return ChatClient.builder(deepseekChatModel).build();
    }

}



