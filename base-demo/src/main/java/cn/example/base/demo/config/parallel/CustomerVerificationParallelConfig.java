package cn.example.base.demo.config.parallel;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;

/**
 * 1、并行客户信息核查智能体
 */
@Configuration
@Import({
        CreditScoreCheckConfig.class,
        OrderSuccessRateConfig.class,
        OrderAveragePriceConfig.class,
        RefundRateCheckConfig.class})
public class CustomerVerificationParallelConfig {

    @Bean
    public ParallelAgent customerVerificationParallelAgent (
            AgentScopeAgent creditScoreCheckAgent,
            AgentScopeAgent orderSuccessRateAgent,
            AgentScopeAgent orderAveragePriceAgent,
            AgentScopeAgent refundRateCheckAgent) {
        return ParallelAgent.builder()
                .name("并行客户信息核查智能体")
                .description("客户信息核查并行处理：信用分、成功率、平均价格、退单率")
                .subAgents(List.of(
                        creditScoreCheckAgent,
                        orderSuccessRateAgent,
                        orderAveragePriceAgent,
                        refundRateCheckAgent))
                .mergeStrategy(new ParallelAgent.DefaultMergeStrategy())
                .build();
    }

}
