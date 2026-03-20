package cn.getech.base.demo.config;

import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.formatter.dashscope.DashScopeMultiAgentFormatter;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 模型配置
 * @author 11030
 */
@Configuration
public class AgentChatModeConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.qwen.model:qwen-plus}")
    private String qwenModel;

    @Bean("qwenAgentChatModel")
    public Model qwenAgentChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName(qwenModel)
                //.baseUrl("https://dashscope.aliyuncs.com")  // 调用地址
                //.enableThinking(true)  // 启用流式输出
                //.formatter(new DashScopeChatFormatter())  // 单智能体：DashScopeChatFormatter，多智能体：DashScopeMultiAgentFormatter
                .build();
    }

}
