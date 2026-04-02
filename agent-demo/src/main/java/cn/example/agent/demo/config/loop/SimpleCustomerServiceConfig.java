package cn.example.agent.demo.config.loop;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleCustomerServiceConfig {

    private static final String CUSTOMER_SERVICE_PROMPT =
            """
            你是一个客服助手。你需要：
            1. 理解用户问题，提供准确回答
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
    public ReactAgent simpleCustomerServiceAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("简单客服助手智能体")
                .description("处理用户咨询多轮对话")
                .model(qwenChatModel)
                .systemPrompt(CUSTOMER_SERVICE_PROMPT)
                .instruction("""
                    用户ID：{userId}
                    会话ID：{sessionId}
                    用户问题：{userMessage}
                    
                    请根据以上信息，给出专业、简洁、有帮助的回答。
                    """)
                .includeContents(false)
                .outputKey("agentResponse")
                .build();
    }

}
