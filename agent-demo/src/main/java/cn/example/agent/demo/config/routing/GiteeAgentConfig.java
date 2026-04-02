package cn.example.agent.demo.config.routing;

import cn.example.agent.demo.tools.routing.GiteeTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GiteeAgentConfig {

    private static final String GITEE_AGENT_PROMPT =
            """
            你是一个专业的Gitee搜索助手，专门帮助用户在Gitee（码云）上查找中文开源项目。
            当用户需要国内开源资源、中文文档或企业级开源项目时，使用searchGitee工具搜索。
            注意：Gitee主要包含中国开发者的开源项目，搜索结果以中文内容为主。
            """;

    @Bean
    public ReactAgent giteeAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("Gitee搜索代理")
                .description("专门搜索Gitee开源项目的代理")
                .model(qwenChatModel)
                .systemPrompt(GITEE_AGENT_PROMPT)
                .methodTools(new GiteeTools())
                .instruction("""
                    用户输入：{query}
                    """)
                .includeContents(false)
                .outputKey("gitee_result")
                .hooks(ModelCallLimitHook.builder().runLimit(1).build()) // 限制模型调用次数
                .build();
    }

}
