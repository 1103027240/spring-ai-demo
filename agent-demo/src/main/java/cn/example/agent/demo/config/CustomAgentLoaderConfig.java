package cn.example.agent.demo.config;

import cn.example.agent.demo.config.sequential.*;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.agent.Agent;
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
            @Qualifier("returnProcessSequentialAgent") SequentialAgent returnProcessSequentialAgent,
            @Qualifier("customerVerificationParallelAgent") ParallelAgent customerVerificationParallelAgent,
            @Qualifier("simpleCustomerChatLoopAgent") LoopAgent simpleCustomerChatLoopAgent) {

        agents.put(RETURN_PROCESS_SEQUENTIAL.getText(), returnProcessSequentialAgent);
        agents.put(CUSTOMER_VERIFICATION_PARALLEL.getText(), customerVerificationParallelAgent);
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
