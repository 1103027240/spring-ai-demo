package cn.example.agent.demo.config.parallel;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static cn.example.agent.demo.enums.AgentNameEnum.CREDIT_SCORE_CHECK;

/**
 * 2、信用分检查智能体
 */
@Configuration
public class CreditScoreCheckConfig {

    private static final String CREDIT_SCORE_CHECK_PROMPT =
            """
            你是一个信用分检查专家。给定客户ID，你需要：
            1. 生成客户的信用分（范围：300-850分）
            2. 根据分数提供评估建议
            3. 返回格式："信用分：{分数}，评估：{评估等级}"
            
            评估等级规则：
            - 750-850：优秀
            - 700-749：良好
            - 650-699：一般
            - 300-649：较差
            
            请生成随机但合理的信用分，不要添加额外文本。
            """;

    @Bean
    public ReactAgent creditScoreCheckAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name(CREDIT_SCORE_CHECK.getText())
                .description("检查客户信用分并提供评估")
                .model(qwenChatModel)
                .systemPrompt(CREDIT_SCORE_CHECK_PROMPT)
                .instruction("""
                    客户ID：{customerId} 
                    检查客户信用分。
                    """)
                .includeContents(false)
                .outputKey("creditScoreResult")
                .build();
    }

}
