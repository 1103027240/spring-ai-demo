package cn.example.agent.demo.loader;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import static cn.example.agent.demo.enums.AgentNameEnum.*;

@Component
public class SimpleCustomerChatLoopAgentLoader extends AbstractAgentLoader {

    public SimpleCustomerChatLoopAgentLoader(@Qualifier("simpleCustomerServiceAgent") ReactAgent simpleCustomerServiceAgent,
                                              @Qualifier("simpleCustomerChatLoopAgent") LoopAgent simpleCustomerChatLoopAgent) {
        agents.put(SIMPLE_CUSTOMER_SERVICE.getText(), simpleCustomerServiceAgent);
        agents.put(SIMPLE_CUSTOMER_CHAT_LOOP.getText(), simpleCustomerChatLoopAgent);
    }

}
