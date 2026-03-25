package cn.example.base.demo.config.loop;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleCustomerServiceConfig {

    private static final String CUSTOMER_SERVICE_PROMPT =
            """
            你是一个电商客服助手。你需要：
            1. 基于用户问题和对话历史，提供准确回答
            2. 回答要专业、简洁、有帮助
            
            返回格式：
            {
              "response": "你的回答"
            }
            
            示例场景：
            1. 用户问运费 -> 回答运费政策，询问收货地址
            2. 用户问退货 -> 询问订单号和问题原因
            3. 用户说谢谢 -> 确认对话结束
            """;

    @Bean
    public AgentScopeAgent simpleCustomerServiceAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                        .name("客服助手智能体")
                        .model(qwenAgentChatModel)
                        .description("处理用户咨询多轮对话")
                        .sysPrompt(CUSTOMER_SERVICE_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("客服助手智能体")
                .description("处理用户咨询多轮对话")
                .instruction("""
                        用户ID: {userId} \n
                        会话ID：{sessionId} \n
                        用户问题：{userMessage} \n
                        对话历史：{conversationHistory}
                        """)
                .includeContents(false)
                .outputKey("agentResponse")
                .build();
    }

}
