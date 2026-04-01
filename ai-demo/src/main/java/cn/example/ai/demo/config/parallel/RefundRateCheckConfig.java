package cn.example.ai.demo.config.parallel;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 5、退款率分析智能体
 */
@Configuration
public class RefundRateCheckConfig {

    private static final String REFUND_RATE_CHECK_PROMPT =
            """
            你是一个退款率分析专家。给定客户ID，你需要：
            1. 计算客户的历史订单退款率（范围：0%-10%）
            2. 根据退款率提供风险评估
            3. 返回格式："退款率：{退款率}%，风险：{风险等级}"
            
            风险等级规则：
            - 0-2%: 低风险
            - 3-5%: 中等风险
            - 6-10%: 高风险
            
            请根据客户ID生成合理的退款率，不要添加额外文本。
            普通客户退款率较高，VIP客户退款率较低。
            """;

    @Bean
    public AgentScopeAgent refundRateCheckAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                        .name("退款率分析智能体")
                        .model(qwenAgentChatModel)
                        .description("分析客户历史订单退款率")
                        .sysPrompt(REFUND_RATE_CHECK_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("退款率分析智能体")
                .description("分析客户历史订单退款率")
                .instruction("""
                        客户ID：{customerId} 
                        分析客户历史订单退款率。
                        """)
                .includeContents(false)
                .outputKey("refundRateCheckResult")
                .build();
    }

}
