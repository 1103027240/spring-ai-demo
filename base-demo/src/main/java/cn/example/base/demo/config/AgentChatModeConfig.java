package cn.example.base.demo.config;

import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static cn.example.base.demo.constant.FieldValueConstant.AGENT_MULTI_MODAL_NAME;

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
        // 配置生成参数优化性能
//        GenerateOptions options = GenerateOptions.builder()
//                .temperature(0.7)          // 降低温度，减少随机性，加快响应
//                .maxTokens(1048)           // 限制最大token数，减少输出时间
//                .topP(0.9)                 // 降低topP，减少计算量
//                .build();

        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName(qwenModel)
                .baseUrl("https://dashscope.aliyuncs.com")
                //.defaultOptions(options)   // 应用优化参数
                //.stream(true)              // 启用流式输出
                .formatter(new DashScopeChatFormatter())
                .build();
    }

    @Bean("qwenMultiModalChatModel")
    public Model qwenMultiModalChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName(AGENT_MULTI_MODAL_NAME)
                .baseUrl("https://dashscope.aliyuncs.com")
                .formatter(new DashScopeChatFormatter())
                //.stream(true)
                .build();
    }

}
