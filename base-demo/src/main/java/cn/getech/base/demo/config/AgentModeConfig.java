package cn.getech.base.demo.config;

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
public class AgentModeConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.qwen.model:qwen-plus}")
    private String qwenModel;

    @Bean("qwenAgentChatModel")
    public Model qwenAgentChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName(qwenModel)
                .build();
    }

}
