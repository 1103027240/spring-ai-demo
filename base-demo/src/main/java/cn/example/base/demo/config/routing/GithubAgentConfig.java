package cn.example.base.demo.config.routing;

import cn.example.base.demo.tools.routing.GitHubTools;
import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GithubAgentConfig {

    private static final String GITHUB_AGENT_PROMPT =
            """
            你是一个专业的GitHub搜索助手，专门帮助用户在GitHub上查找开源代码、项目和仓库。
            当用户提出技术问题或需要代码示例时，使用search_github工具搜索相关资源。
            注意：GitHub主要包含国际开源项目，搜索结果以英文内容为主。
            """;

    @Bean
    public AgentScopeAgent githubAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new GitHubTools());

        ReActAgent.Builder reactBuilder = ReActAgent.builder()
                .name("GitHub搜索代理")
                .model(qwenAgentChatModel)
                .sysPrompt(GITHUB_AGENT_PROMPT)
                .toolkit(toolkit);

        return AgentScopeAgent.fromBuilder(reactBuilder)
                .name("GitHub搜索代理")
                .description("专门搜索GitHub开源项目的代理")
                .instruction("""
                        用户输入：{query}
                        """)
                .includeContents(true)
                .returnReasoningContents(false)
                .outputKey("github_result")
                .build();
    }

}
