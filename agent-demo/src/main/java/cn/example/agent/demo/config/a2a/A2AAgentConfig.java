package cn.example.agent.demo.config.a2a;

import com.alibaba.cloud.ai.a2a.registry.nacos.discovery.NacosAgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.nacos.api.ai.A2aService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class A2AAgentConfig {

    @Autowired(required = false)
    private A2aService a2aService;

    /**
     * NacosAgentCardProvider
     */
    @Bean
    public AgentCardProvider agentCardProvider() {
        if (a2aService != null) {
            return new NacosAgentCardProvider(a2aService);
        }
        throw new IllegalStateException("A2aService not available, please check Nacos configuration");
    }

    @Primary
    @Bean(name = "dataAnalysisAgent")
    public ReactAgent dataAnalysisAgent(@Qualifier("qwenChatModel") ChatModel qwenChatModel) {
        return ReactAgent.builder()
                .name("data_analysis_agent")
                .model(qwenChatModel)
                .description("专门用于数据分析和统计计算的本地智能体")
                .instruction("你是一个专业的数据分析专家，擅长处理各类数据统计和分析任务。能够理解用户的数据分析需求，提供准确的统计计算结果和专业的分析建议。")
                .outputKey("messages")
                .build();
    }

}
