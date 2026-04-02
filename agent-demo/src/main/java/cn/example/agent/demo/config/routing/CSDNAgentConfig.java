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
            
            重要：你必须使用searchCSDN工具来搜索CSDN上的资源。
            请立即调用searchCSDN工具，返回搜索结果。
            不要询问用户更多问题，不要输出提示文字，直接返回工具结果。
            
            CSDN主要包含中文技术博客和文章，适合学习和技术问题解答。
            """;

    @Bean
    public ReactAgent csdnAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("csdn")
                .description("专门搜索CSDN技术文章的代理")
                .model(qwenChatModel)
                .systemPrompt(CSDN_AGENT_PROMPT)
                .methodTools(new CSDNTools())
                .instruction("用户查询：{csdn_input}")
                .includeContents(false)
                .outputKey("csdn_result")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build()) // 限制模型调用次数
                .build();
    }

}
