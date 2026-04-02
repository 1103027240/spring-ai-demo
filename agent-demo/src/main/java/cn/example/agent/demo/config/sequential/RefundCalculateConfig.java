package cn.example.agent.demo.config.sequential;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static cn.example.agent.demo.enums.AgentNameEnum.REFUND_CALCULATE;

/**
 * 4、退款计算智能体
 */
@Configuration
public class RefundCalculateConfig {

    private static final String REFUND_CALCULATE_PROMPT =
            """
            你是一个退款计算器。给定订单金额和政策检查结果，你需要：
            1. 如果政策检查结果不是"符合条件"，返回"无退款：{政策结果}"
            2. 如果政策检查结果是"符合条件"，计算退款金额为订单金额的80%。返回"退款金额：{退款金额}，订单金额：{订单金额}" 
            
            只返回上述响应之一，不要添加额外文本等。
            """;

    @Bean
    public ReactAgent refundCalculateAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name(REFUND_CALCULATE.getText())
                .description("为符合条件的订单计算退款金额")
                .model(qwenChatModel)
                .systemPrompt(REFUND_CALCULATE_PROMPT)
                .instruction(
                    """
                    订单金额：{amount} 
                    政策检查结果: {policyCheckResult} 
                    如果适用，计算退款金额。
                    """)
                .includeContents(false)
                .outputKey("refundAmount")
                .build();
    }

}
