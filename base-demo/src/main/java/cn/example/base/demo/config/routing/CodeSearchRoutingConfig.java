package cn.example.base.demo.config.routing;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CodeSearchRoutingConfig {

    @Bean
    public AgentScopeRoutingAgent codeSearchRoutingAgent (
            @Qualifier("githubAgent") AgentScopeAgent githubAgent,
            @Qualifier("giteeAgent") AgentScopeAgent giteeAgent,
            @Qualifier("csdnAgent") AgentScopeAgent csdnAgent,
            @Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        return AgentScopeRoutingAgent.builder()
                .name("代码智能搜索代理")
                .model(qwenAgentChatModel)
                .description("根据查询内容智能路由到GitHub、Gitee、CSDN搜索代理，可同时调用一个或多个子代理")
                .systemPrompt(
                    """
                    你是一个智能路由代理，负责分析用户查询并决定调用哪些搜索代理。
                    
                    可选择的代理名称（必须使用以下完整名称）：
                    - GitHub搜索代理
                    - Gitee搜索代理
                    - CSDN搜索代理
                    
                    路由规则：
                    1. 如果查询包含"GitHub"、"英文"、"国际"、"开源"等关键词，选择 GitHub搜索代理
                    2. 如果查询包含"Gitee"、"中文"、"国内"、"企业"等关键词，选择 Gitee搜索代理  
                    3. 如果查询包含"博客"、"文章"、"教程"、"学习"等关键词，选择 CSDN搜索代理
                    4. 对于通用技术查询，可以同时选择多个代理获取全面结果
                    
                    请根据用户查询的语义内容选择需要调用的代理。
                    """)
                .subAgents(List.of(githubAgent, giteeAgent, csdnAgent))
                .build();
    }

}
