package cn.example.base.demo.config.demo;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单校验器Agent
 */
 @Configuration
public class OrderValidConfig {

    private static final String ORDER_GENERATION_PROMPT  =
        """
        你是一个订单校验专家。给定一个订单ID。你需要：
        1. 校验订单是否存在
        2. 校验订单状态是否是"已发货"
        
        如果订单不存在，返回"订单不存在"
        如果订单存在但订单状态不是"已发货"，返回"订单状态无效：{当前状态}"
        如果订单存在且订单状态为"已发货"，返回"验证通过"
        
        只返回上述三种响应之一，不要添加额外文本等
        """;

    @Bean("orderValidAgent")
    public AgentScopeAgent orderValidAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel){
        ReActAgent.Builder builder = ReActAgent.builder()
                .name("订单校验器")
                .model(qwenAgentChatModel)
                .description("校验订单是否存在且状态为已发货")
                .sysPrompt(ORDER_GENERATION_PROMPT);

        return AgentScopeAgent.fromBuilder(builder)
                .name("订单校验器")
                .description("校验订单是否存在且状态为已发货")
                .includeContents(false)  //是否将上下文、历史对话传入提示词（默认是true），如果只要instruction，设为false
                .instruction("校验订单ID：{orderId}")
                .outputKey("orderValidResult")
                .build();
    }

}
