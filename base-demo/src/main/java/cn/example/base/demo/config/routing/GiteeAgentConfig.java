package cn.example.base.demo.config.routing;

import cn.example.base.demo.tools.routing.GiteeTools;
import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GiteeAgentConfig {

    private static final String GITEE_AGENT_PROMPT =
            """
            你是一个专业的Gitee搜索助手，专门帮助用户在Gitee（码云）上查找中文开源项目。
            当用户需要国内开源资源、中文文档或企业级开源项目时，使用search_gitee工具搜索。
            注意：Gitee主要包含中国开发者的开源项目，搜索结果以中文内容为主。
            """;

    @Bean
    public AgentScopeAgent giteeAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new GiteeTools());

        ReActAgent.Builder reactBuilder = ReActAgent.builder()
                .name("Gitee搜索代理")
                .model(qwenAgentChatModel)
                .sysPrompt(GITEE_AGENT_PROMPT)
                .toolkit(toolkit);

        return AgentScopeAgent.fromBuilder(reactBuilder)
                .name("Gitee搜索代理")
                .description("专门搜索Gitee开源项目的代理")
                .instruction("""
                        用户输入：{query}
                        """)
                .includeContents(true)
                .returnReasoningContents(false)
                .outputKey("gitee_result")
                .build();
    }

}
