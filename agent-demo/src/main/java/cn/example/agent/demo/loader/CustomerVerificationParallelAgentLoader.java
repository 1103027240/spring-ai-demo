package cn.example.agent.demo.loader;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import static cn.example.agent.demo.enums.AgentNameEnum.*;

@Component
public class CustomerVerificationParallelAgentLoader extends AbstractAgentLoader {

    public CustomerVerificationParallelAgentLoader(@Qualifier("creditScoreCheckAgent") ReactAgent creditScoreCheckAgent,
                                                   @Qualifier("orderSuccessRateAgent") ReactAgent orderSuccessRateAgent,
                                                   @Qualifier("averageOrderValueAgent") ReactAgent orderAveragePriceAgent,
                                                   @Qualifier("refundRateCheckAgent") ReactAgent refundRateCheckAgent,
                                                   @Qualifier("refundRateCheckAgent") ReactAgent customerVerificationParallelAgent) {
        agents.put(CREDIT_SCORE_CHECK.getText(), creditScoreCheckAgent);
        agents.put(ORDER_SUCCESS_RATE.getText(), orderSuccessRateAgent);
        agents.put(AVERAGE_ORDER_VALUE.getText(), orderAveragePriceAgent);
        agents.put(REFUND_RATE_CHECK.getText(), refundRateCheckAgent);
        agents.put(CUSTOMER_VERIFICATION_PARALLEL.getText(), customerVerificationParallelAgent);
    }

}
