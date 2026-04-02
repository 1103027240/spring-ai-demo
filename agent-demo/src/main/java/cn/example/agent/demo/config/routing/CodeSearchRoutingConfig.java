package cn.example.agent.demo.config.routing;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CodeSearchRoutingConfig {

    @Bean
    public LlmRoutingAgent codeSearchRoutingAgent (
            @Qualifier("githubAgent") ReactAgent githubAgent,
            @Qualifier("giteeAgent") ReactAgent giteeAgent,
            @Qualifier("csdnAgent") ReactAgent csdnAgent,
            @Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return LlmRoutingAgent.builder()
                .name("代码智能搜索代理")
                .description("根据查询内容智能路由到GitHub、Gitee、CSDN仅且其中一个查询代理")
                .model(qwenChatModel)
                .systemPrompt(
                    """
                    你是一个智能路由代理，负责分析用户查询并决定调用哪个查询代理。
                    
                    可选择的代理名称：
                    - github_agent
                    - gitee_agent
                    - csdn_agent
                    
                    路由规则：
                    1. 如果查询包含"GitHub"、"英文"、"国际"、"开源"等关键词，优先选择 github_agent
                    2. 如果查询包含"Gitee"、"中文"、"国内"、"企业"等关键词，优先选择 gitee_agent
                    3. 如果查询包含"博客"、"文章"、"教程"、"学习"等关键词，优先选择 csdn_agent
                    4. 对于通用技术查询，选择 github_agent
                    
                    请根据用户查询的语义内容选择需要调用的代理，并请用中文回答用户的问题。
                    """)
                .subAgents(List.of(githubAgent, giteeAgent, csdnAgent))
                .build();
    }

}
