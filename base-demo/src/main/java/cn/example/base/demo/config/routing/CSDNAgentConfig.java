package cn.example.base.demo.config.routing;

import cn.example.base.demo.tools.routing.CSDNTools;
import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CSDNAgentConfig {

    private static final String CSDN_AGENT_PROMPT =
            """
            你是一个专业的CSDN搜索助手，专门帮助用户查找技术文章、博客和教程。
            当用户需要学习资料、技术文档或解决方案时，使用search_csdn工具搜索。
            注意：CSDN主要包含中文技术博客和文章，适合学习和技术问题解答。
            """;

    @Bean
    public AgentScopeAgent csdnAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new CSDNTools());

        ReActAgent.Builder reactBuilder = ReActAgent.builder()
                .name("CSDN搜索代理")
                .model(qwenAgentChatModel)
                .sysPrompt(CSDN_AGENT_PROMPT)
                .toolkit(toolkit);

        return AgentScopeAgent.fromBuilder(reactBuilder)
                .name("CSDN搜索代理")
                .description("专门搜索CSDN技术文章的代理")
                .includeContents(true)
                .returnReasoningContents(false)
                .outputKey("csdn_result")
                .build();
    }

}
