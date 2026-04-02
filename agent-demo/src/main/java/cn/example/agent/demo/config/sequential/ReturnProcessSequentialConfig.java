package cn.example.agent.demo.config.sequential;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;
import static cn.example.agent.demo.enums.AgentNameEnum.RETURN_PROCESS_SEQUENTIAL;

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
            @Qualifier("returnOrderCheckAgent") ReactAgent returnOrderCheckAgent,
            @Qualifier("returnPolicyCheckAgent") ReactAgent returnPolicyCheckAgent,
            @Qualifier("refundCalculateAgent") ReactAgent refundCalculateAgent,
            @Qualifier("returnOrderGenerateAgent") ReactAgent returnOrderGenerateAgent) {
        return SequentialAgent.builder()
                .name(RETURN_PROCESS_SEQUENTIAL.getText())
                .description("退货处理顺序执行：验证订单、检查政策、计算退款、生成退货单")
                .subAgents(List.of(
                        returnOrderCheckAgent,
                        returnPolicyCheckAgent,
                        refundCalculateAgent,
                        returnOrderGenerateAgent))
                .build();
    }

}
