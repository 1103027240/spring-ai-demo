package cn.example.agent.demo.config.parallel;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;
import static cn.example.agent.demo.enums.AgentNameEnum.CUSTOMER_VERIFICATION_PARALLEL;

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
    public ParallelAgent customerVerificationParallelAgent(
            @Qualifier("creditScoreCheckAgent") ReactAgent creditScoreCheckAgent,
            @Qualifier("orderSuccessRateAgent") ReactAgent orderSuccessRateAgent,
            @Qualifier("averageOrderValueAgent") ReactAgent orderAveragePriceAgent,
            @Qualifier("refundRateCheckAgent") ReactAgent refundRateCheckAgent) {
        return ParallelAgent.builder()
                .name(CUSTOMER_VERIFICATION_PARALLEL.getText())
                .description("客户信息核查并行执行：信用分、成功率、平均价格、退单率")
                .subAgents(List.of(
                        creditScoreCheckAgent,
                        orderSuccessRateAgent,
                        orderAveragePriceAgent,
                        refundRateCheckAgent))
                .mergeStrategy(new ParallelAgent.DefaultMergeStrategy())
                .mergeOutputKey("customerVerificationResult")
                .maxConcurrency(4)
                .build();
    }

}
