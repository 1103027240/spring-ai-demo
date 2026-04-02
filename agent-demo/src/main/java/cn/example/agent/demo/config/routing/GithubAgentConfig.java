package cn.example.agent.demo.config.routing;

import cn.example.agent.demo.tools.routing.GitHubTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GithubAgentConfig {

    private static final String GITHUB_AGENT_PROMPT =
            """
            你是一个专业的GitHub搜索助手，专门帮助用户在GitHub上查找开源代码、项目和仓库。
            当用户提出技术问题或需要代码示例时，使用searchGitHub工具搜索相关资源。
            注意：GitHub主要包含国际开源项目，搜索结果以英文内容为主。
            """;

    @Bean
    public ReactAgent githubAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("GitHub搜索代理")
                .description("专门搜索GitHub开源项目的代理")
                .model(qwenChatModel)
                .systemPrompt(GITHUB_AGENT_PROMPT)
                .methodTools(new GitHubTools())
                .instruction("""
                    用户输入：{query}
                    """)
                .includeContents(false)
                .outputKey("github_result")
                .hooks(ModelCallLimitHook.builder().runLimit(1).build()) // 限制模型调用次数
                .build();
    }

}
