package cn.example.agent.demo.config.routing;

import cn.example.agent.demo.tools.routing.CSDNTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CSDNAgentConfig {

    private static final String CSDN_AGENT_PROMPT =
            """
            你是一个专业的CSDN搜索助手，专门帮助用户查找技术文章、博客和教程。
            当用户需要学习资料、技术文档或解决方案时，使用searchCSDN工具搜索。
            注意：CSDN主要包含中文技术博客和文章，适合学习和技术问题解答。
            """;

    @Bean
    public ReactAgent csdnAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("CSDN搜索代理")
                .description("专门搜索CSDN技术文章的代理")
                .model(qwenChatModel)
                .systemPrompt(CSDN_AGENT_PROMPT)
                .methodTools(new CSDNTools())
                .instruction("""
                    用户输入：{query}
                    """)
                .includeContents(false)
                .outputKey("csdn_result")
                .hooks(ModelCallLimitHook.builder().runLimit(1).build()) // 限制模型调用次数
                .build();
    }

}
