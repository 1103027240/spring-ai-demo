package cn.example.base.demo.config.loop;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.LoopMode;

public class SimpleCustomerChatLoopConfig {

    public LoopAgent getSimpleCustomerChatLoopAgent(AgentScopeAgent simpleCustomerServiceAgent) {
        return LoopAgent.builder()
                .name("客服对话循环智能体")
                .description("客服对话多轮执行，直到问题解决")
                .subAgent(simpleCustomerServiceAgent)
                .loopStrategy(LoopMode.condition(new SimpleCustomerChatLoopCondition()))  //先执行subAgent，再执行loopStrategy
                .build();
    }

}
