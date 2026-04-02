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
                .description("根据查询内容智能路由到GitHub、Gitee、CSDN搜索代理，可同时调用多个子代理")
                .model(qwenChatModel)
                .systemPrompt(
                    """
                    你是一个智能路由代理，负责分析用户查询并决定调用哪些搜索代理。
                    
                    可选择的代理名称（必须使用以下完整名称）：
                    - github
                    - gitee
                    - csdn
                    
                    路由规则：
                    1. 如果查询包含"GitHub"、"英文"、"国际"、"开源"等关键词，选择 github
                    2. 如果查询包含"Gitee"、"中文"、"国内"、"企业"等关键词，选择 gitee
                    3. 如果查询包含"博客"、"文章"、"教程"、"学习"等关键词，选择 csdn
                    4. 对于通用技术查询，可以同时选择多个代理获取全面结果
                    
                    输出格式要求（严格遵循以下格式，不要输出任何其他内容）：
                    query字段必须填入用户的实际查询内容，例如：
                    单个代理示例：
                    {"agents":[{"agent":"github","query":"前后端分离架构最佳实践"}]}
                    多个代理示例：
                    {"agents":[{"agent":"github","query":"前后端分离架构最佳实践"},{"agent":"gitee","query":"前后端分离架构最佳实践"}]}
                    
                    注意：只输出一行JSON对象，不要加markdown代码块标记，query必须是实际查询文本。
                    """)
                .subAgents(List.of(githubAgent, giteeAgent, csdnAgent))
                .build();
    }

}
