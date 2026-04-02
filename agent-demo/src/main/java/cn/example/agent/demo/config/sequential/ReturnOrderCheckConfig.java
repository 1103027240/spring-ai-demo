package cn.example.agent.demo.config.sequential;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static cn.example.agent.demo.enums.AgentNameEnum.RETURN_ORDER_CHECK;

/**
 * 2、订单验证智能体
 */
@Configuration
public class ReturnOrderCheckConfig {

    private static final String ORDER_CHECK_PROMPT  =
        """
        你是一个订单验证专家。给定一个订单ID和订单状态。你需要：
        1. 校验订单是否存在
        2. 校验订单状态是否是"Shipped"。
        
        订单状态说明：
        - Paid: 已支付
        - Shipped: 已发货
        - Delivered: 已送达
        - 其他值：未知状态
        
        如果订单不存在，返回"订单不存在"
        如果订单存在且订单状态不是"Shipped"，返回"订单状态：{订单状态中文描述}，不能退货"
        如果订单存在且订单状态是"Shipped"，返回"验证通过"
        
        只返回上述响应之一，不要添加额外文本等
        """;

    @Bean
    public ReactAgent returnOrderCheckAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name(RETURN_ORDER_CHECK.getText())
                .description("校验订单是否存在且状态为Shipped")
                .model(qwenChatModel)
                .systemPrompt(ORDER_CHECK_PROMPT)
                .instruction("""
                    订单ID：{orderId} 
                    订单状态：{status}
                    检查订单和状态。
                    """)
                .includeContents(false)  //默认true，表示将上下文消息（Msg）也传入提示词；设为false，只有instruction参数；不管是true/false，都能从state获取数据
                .outputKey("orderCheckResult")
                .build();
    }

}
