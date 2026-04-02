package cn.example.agent.demo.config;

import cn.example.agent.demo.config.sequential.*;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import static cn.example.agent.demo.enums.AgentNameEnum.*;

@Configuration
public class CustomAgentLoaderConfig {

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();

    @Bean
    public AgentLoader customAgentLoader (
            @Qualifier("returnOrderCheckAgent") ReactAgent returnOrderCheckAgent,
            @Qualifier("returnPolicyCheckAgent") ReactAgent returnPolicyCheckAgent,
            @Qualifier("refundCalculateAgent") ReactAgent refundCalculateAgent,
            @Qualifier("returnOrderGenerateAgent") ReactAgent returnOrderGenerateAgent,
            @Qualifier("returnProcessSequentialAgent") SequentialAgent returnProcessSequentialAgent,

            @Qualifier("creditScoreCheckAgent") ReactAgent creditScoreCheckAgent,
            @Qualifier("orderSuccessRateAgent") ReactAgent orderSuccessRateAgent,
            @Qualifier("averageOrderValueAgent") ReactAgent orderAveragePriceAgent,
            @Qualifier("refundRateCheckAgent") ReactAgent refundRateCheckAgent,
            @Qualifier("customerVerificationParallelAgent") ParallelAgent customerVerificationParallelAgent,

            @Qualifier("simpleCustomerServiceAgent") ReactAgent simpleCustomerServiceAgent,
            @Qualifier("simpleCustomerChatLoopAgent") LoopAgent simpleCustomerChatLoopAgent) {

        agents.put(RETURN_ORDER_CHECK.getText(), returnOrderCheckAgent);
        agents.put(RETURN_POLICY_CHECK.getText(), returnPolicyCheckAgent);
        agents.put(REFUND_CALCULATE.getText(), refundCalculateAgent);
        agents.put(RETURN_ORDER_GENERATE.getText(), returnOrderGenerateAgent);
        agents.put(RETURN_PROCESS_SEQUENTIAL.getText(), returnProcessSequentialAgent);

        agents.put(CREDIT_SCORE_CHECK.getText(), creditScoreCheckAgent);
        agents.put(ORDER_SUCCESS_RATE.getText(), orderSuccessRateAgent);
        agents.put(AVERAGE_ORDER_VALUE.getText(), orderAveragePriceAgent);
        agents.put(REFUND_RATE_CHECK.getText(), refundRateCheckAgent);
        agents.put(CUSTOMER_VERIFICATION_PARALLEL.getText(), customerVerificationParallelAgent);

        agents.put(SIMPLE_CUSTOMER_SERVICE.getText(), simpleCustomerServiceAgent);
        agents.put(SIMPLE_CUSTOMER_CHAT_LOOP.getText(), simpleCustomerChatLoopAgent);

        return new AgentLoader() {
            @Override
            public @NonNull List<String> listAgents() {
                return agents.keySet().stream().toList();
            }

            @Override
            public Agent loadAgent(String name) {
                if (StrUtil.isBlank(name)) {
                    throw new IllegalArgumentException("Agent name cannot be null");
                }

                Agent agent = agents.get(name);
                if (agent == null) {
                    throw new NoSuchElementException("Agent not found: " + name);
                }
                return agent;
            }
        };
    }

}
