package cn.example.ai.demo.config.parallel;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 4、客单价分析智能体
 */
@Configuration
public class OrderAveragePriceConfig {

    private static final String AVERAGE_ORDER_VALUE_PROMPT =
            """
            你是一个客单价分析专家。给定客户ID，你需要：
            1. 计算客户的历史平均客单价（范围：100-1000元）
            2. 提供客单价水平评估
            3. 返回格式："平均客单价：¥{金额}，水平：{评估水平}"
            
            评估水平规则：
            - ¥800-1000：高价值客户
            - ¥500-799：中等价值客户
            - ¥100-499：低价值客户
            
            请根据客户ID生成合理的客单价，不要添加额外文本。
            """;

    @Bean
    public AgentScopeAgent averageOrderValueAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                        .name("客单价分析智能体")
                        .model(qwenAgentChatModel)
                        .description("分析客户历史平均客单价")
                        .sysPrompt(AVERAGE_ORDER_VALUE_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("客单价分析智能体")
                .description("分析客户历史平均客单价")
                .instruction("""
                        客户ID：{customerId} 
                        分析客户历史平均客单价。
                        """)
                .includeContents(false)
                .outputKey("orderAveragePriceResult")
                .build();
    }

}
