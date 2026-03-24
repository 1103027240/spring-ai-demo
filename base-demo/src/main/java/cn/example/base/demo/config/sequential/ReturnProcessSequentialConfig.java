package cn.example.base.demo.config.sequential;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;

/**
 * 5、顺序退货流程处理智能体
 */
@Configuration
@Import({
        OrderCheckConfig.class,
        ReturnPolicyCheckConfig.class,
        RefundCalculateConfig.class,
        ReturnOrderGenerateConfig.class})
public class ReturnProcessSequentialConfig {

    @Bean("returnProcessSequentialAgent")
    public SequentialAgent returnProcessSequentialAgent(
            AgentScopeAgent orderCheckAgent,
            AgentScopeAgent returnPolicyCheckAgent,
            AgentScopeAgent refundCalculateAgent,
            AgentScopeAgent returnOrderGenerateAgent) {

        return SequentialAgent.builder()
                .name("顺序退货流程处理器")
                .description("电商退货流程管道：验证订单、检查政策、计算退款、生成退货单")
                .subAgents(List.of(
                        orderCheckAgent,
                        returnPolicyCheckAgent,
                        refundCalculateAgent,
                        returnOrderGenerateAgent))
                .build();
    }

}
