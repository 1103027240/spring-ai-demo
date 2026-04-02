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
            
            重要：你必须使用searchGitHub工具来搜索GitHub上的资源。
            请立即调用searchGitHub工具，返回搜索结果。
            不要询问用户更多问题，不要输出提示文字，直接返回工具结果。
            
            GitHub主要包含国际开源项目，搜索结果以英文内容为主。
            """;

    @Bean
    public ReactAgent githubAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("github")
                .description("专门搜索GitHub开源项目的代理")
                .model(qwenChatModel)
                .systemPrompt(GITHUB_AGENT_PROMPT)
                .methodTools(new GitHubTools())
                .instruction("用户查询：{github_input}")
                .includeContents(false)
                .outputKey("github_result")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build()) // 限制模型调用次数
                .build();
    }

}
