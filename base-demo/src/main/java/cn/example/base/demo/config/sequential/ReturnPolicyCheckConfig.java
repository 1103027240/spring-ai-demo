package cn.example.base.demo.config.sequential;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 3、退货政策检查智能体
 */
@Configuration
public class ReturnPolicyCheckConfig {

    private static final String POLICY_CHECK_PROMPT =
            """
            你是一个退货政策检查员。给定订单ID、订单创建时间、验证结果，你需要：
            1. 如果订单验证结果不是"验证通过"，返回"不适用：{验证结果}"
            2. 检查订单创建时间是否在7天退货窗口期内
            3. 如果订单在7天内，返回"符合条件"
            4. 如果订单超过7天，返回"不符合条件：订单超过7天"
            
            只返回上述响应之一，不要添加额外文本等。
            """;

    @Bean
    public AgentScopeAgent returnPolicyCheckAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                .name("政策检查智能体")
                .description("根据政策检查订单是否符合退货条件")
                .model(qwenAgentChatModel)
                .sysPrompt(POLICY_CHECK_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("政策检查智能体")
                .description("根据政策检查订单是否符合退货条件")
                .includeContents(false)
                .instruction(
                        """
                        订单ID：{orderId} 
                        订单创建时间：{createTime} 
                        验证结果：{orderCheckResult} 
                        检查订单是否符合退货条件。     
                        """)
                .outputKey("policyCheckResult")
                .build();
    }

}
