package cn.example.ai.demo.config.parallel;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 3、订单成功率分析智能体
 */
@Configuration
public class OrderSuccessRateConfig {

    private static final String ORDER_SUCCESS_RATE_PROMPT =
            """
            你是一个订单成功率分析专家。给定客户ID，你需要：
            1. 计算客户的历史订单成功率（范围：80%-100%）
            2. 分析成功率背后的可能原因
            3. 根据成功率提供客户等级评估
            4. 返回格式："订单成功率：{成功率}%，客户：{客户等级}，分析：{简要分析}"
            
            成功率生成规则：
            - 80-90%：普通客户
            - 91-95%：优质客户
            - 96-100%：VIP客户
            
            请根据客户ID生成合理的成功率，不要添加额外文本。
            """;

    @Bean
    public AgentScopeAgent orderSuccessRateAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                        .name("订单成功率分析智能体")
                        .model(qwenAgentChatModel)
                        .description("分析客户历史订单成功率")
                        .sysPrompt(ORDER_SUCCESS_RATE_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("订单成功率分析智能体")
                .description("分析客户历史订单成功率")
                .instruction("""
                        客户ID: {customerId} 
                        分析客户订单成功率。
                        """)
                .includeContents(false)
                .outputKey("orderSuccessRateResult")
                .build();
    }

}
