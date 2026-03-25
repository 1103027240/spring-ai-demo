package cn.example.base.demo.config.loop;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.LoopMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleChatLoopConfig {

    @Bean
    public LoopAgent simpleChatLoopAgent(@Qualifier("simpleCustomerServiceAgent") AgentScopeAgent simpleCustomerServiceAgent) {
        return LoopAgent.builder()
                .name("客服对话循环智能体")
                .description("处理客服多轮对话，直到问题解决")
                .subAgent(simpleCustomerServiceAgent)
                .loopStrategy(LoopMode.condition(new SimpleCustomerServiceLoopCondition()))
                .build();
    }

}
