package cn.example.agent.demo.config.loop;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.LoopMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static cn.example.agent.demo.enums.AgentNameEnum.SIMPLE_CUSTOMER_CHAT_LOOP;

@Configuration
@Import({SimpleCustomerServiceConfig.class})
public class SimpleCustomerChatLoopConfig {

    @Bean
    public LoopAgent simpleCustomerChatLoopAgent(@Qualifier("simpleCustomerServiceAgent") ReactAgent simpleCustomerServiceAgent) {
        return LoopAgent.builder()
                .name(SIMPLE_CUSTOMER_CHAT_LOOP.getText())
                .description("客服对话多轮执行，直到问题解决")
                .subAgent(simpleCustomerServiceAgent)
                .loopStrategy(LoopMode.condition(new SimpleCustomerChatLoopCondition()))  //先执行subAgent，再执行loopStrategy
                .build();
    }

}
