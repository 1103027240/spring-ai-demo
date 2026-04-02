package cn.example.agent.demo.config.sequential;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static cn.example.agent.demo.enums.AgentNameEnum.RETURN_ORDER_GENERATE;

/**
 * 5、退货单生成智能体
 */
@Configuration
public class ReturnOrderGenerateConfig {

    private static final String RETURN_ORDER_GENERATE_PROMPT =
            """
            你是一个退货单生成器。根据前几步的结果，你需要：
            1. 检查是否有可用的退款金额（以"退款金额："开头）
            2. 如果有退款，生成退货单号，格式："RT" + 时间戳。返回"退货单：{退货单号}，状态：待审核"
            3. 如果没有退款，返回"无退货单：{退款结果}"
            
            只返回上述响应之一，不要添加额外文本等。
            时间戳使用当前时间的毫秒数。
            """;

    @Bean
    public ReactAgent returnOrderGenerateAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name(RETURN_ORDER_GENERATE.getText())
                .description("为符合条件的退款生成退货单")
                .model(qwenChatModel)
                .systemPrompt(RETURN_ORDER_GENERATE_PROMPT)
                .instruction(
                    """
                    订单ID: {orderId} 
                    验证结果: {orderCheckResult} 
                    政策结果: {orderCheckResult} 
                    退款金额: {refundAmount} 
                    如果适用，生成退货单。
                    """)
                .includeContents(false)
                .outputKey("returnOrder")
                .build();
    }

}
