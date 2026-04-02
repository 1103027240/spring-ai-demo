package cn.example.ai.demo.config.sequential;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;

/**
 * 1、顺序退货流程处理智能体
 */
@Configuration
@Import({
        ReturnOrderCheckConfig.class,
        ReturnPolicyCheckConfig.class,
        RefundCalculateConfig.class,
        ReturnOrderGenerateConfig.class})
public class ReturnProcessSequentialConfig {

    @Bean
    public SequentialAgent returnProcessSequentialAgent (
            @Qualifier("returnOrderCheckAgent") AgentScopeAgent returnOrderCheckAgent,
            @Qualifier("returnPolicyCheckAgent") AgentScopeAgent returnPolicyCheckAgent,
            @Qualifier("refundCalculateAgent") AgentScopeAgent refundCalculateAgent,
            @Qualifier("returnOrderGenerateAgent") AgentScopeAgent returnOrderGenerateAgent) {
        return SequentialAgent.builder()
                .name("退货处理顺序智能体")
                .description("退货处理顺序执行：验证订单、检查政策、计算退款、生成退货单")
                .subAgents(List.of(
                        returnOrderCheckAgent,
                        returnPolicyCheckAgent,
                        refundCalculateAgent,
                        returnOrderGenerateAgent))
                .build();
    }

}
