package cn.example.base.demo.config.sequential;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2、订单验证智能体
 */
@Configuration
public class OrderCheckConfig {

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
    public AgentScopeAgent orderCheckAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                .name("订单验证智能体")
                .model(qwenAgentChatModel)
                .description("校验订单是否存在且状态为Shipped")
                .sysPrompt(ORDER_CHECK_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("订单验证智能体")
                .description("校验订单是否存在且状态为Shipped")
                .includeContents(false)  //是否将状态数据OverAllState传入提示词（默认是true），如果只要instruction，设为false
                .instruction("""
                        订单ID：{orderId} 
                        订单状态：{status}
                        检查订单和状态。
                        """)
                .outputKey("orderCheckResult")
                .build();
    }

}
