package cn.example.agent.demo.loader;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import static cn.example.agent.demo.enums.AgentNameEnum.*;

@Component
public class ReturnProcessSequentialAgentLoader extends AbstractAgentLoader {

    public ReturnProcessSequentialAgentLoader(@Qualifier("returnOrderCheckAgent") ReactAgent returnOrderCheckAgent,
                                              @Qualifier("returnPolicyCheckAgent") ReactAgent returnPolicyCheckAgent,
                                              @Qualifier("refundCalculateAgent") ReactAgent refundCalculateAgent,
                                              @Qualifier("returnOrderGenerateAgent") ReactAgent returnOrderGenerateAgent,
                                              @Qualifier("returnOrderGenerateAgent") ReactAgent returnProcessSequentialAgent) {
        agents.put(RETURN_ORDER_CHECK.getText(), returnOrderCheckAgent);
        agents.put(RETURN_POLICY_CHECK.getText(), returnPolicyCheckAgent);
        agents.put(REFUND_CALCULATE.getText(), refundCalculateAgent);
        agents.put(RETURN_ORDER_GENERATE.getText(), returnOrderGenerateAgent);
        agents.put(RETURN_PROCESS_SEQUENTIAL.getText(), returnProcessSequentialAgent);
    }

}
