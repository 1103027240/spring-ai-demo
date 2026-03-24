package cn.example.base.demo.config.sequential;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 3、退款计算智能体
 */
@Configuration
public class RefundCalculateConfig {

    private static final String REFUND_CALCULATE_PROMPT =
            """
            你是一个退款计算器。给定政策检查结果，你需要：
            1. 如果政策检查结果不是"符合条件"，返回"无退款: {政策结果}"
            2. 如果政策检查结果是"符合条件"，计算退款金额为原金额的80%
            3. 格式化返回: "退款金额: ${金额}" (例如订单原金额：199.8，退款金额：199.8的80%是159.84)
            
            只返回格式化后的响应，不要添加额外文本等。
            假设符合条件的订单原金额在1 ~ 1000随机生成整数。
            """;

    @Bean("refundCalculateAgent")
    public AgentScopeAgent refundCalculateAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                        .name("退款计算器")
                        .model(qwenAgentChatModel)
                        .description("为符合条件的订单计算退款金额")
                        .sysPrompt(REFUND_CALCULATE_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("退款计算器")
                .description("为符合条件的订单计算退款金额")
                .includeContents(false)
                .instruction(
                        """
                        政策检查结果: {policyCheckResult} \n
                        如果适用，计算退款金额。
                        """)
                .outputKey("refundAmount")
                .build();
    }

}
