package cn.example.base.demo.agent;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.internal.node.Node;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqlResultAnalysisAgent {

    @Getter
    private AgentScopeAgent agentScopeAgent;

    private static final String QUERY_ANALYSIS_PROMPT = """
            你是一个数据分析智能体。你的职责是：
            1. 分析SQL查询结果数据
            2. 提供业务洞察和建议
            3. 识别数据中的模式和趋势
            4. 生成简洁的分析报告
            
            请基于提供的查询结果数据，提供有价值的业务分析。
            
            分析报告应包含：
            1. 数据概况
            2. 关键发现
            3. 业务建议
            4. 潜在风险
            
            响应格式必须是JSON：
            {
                "success": true/false,
                "analysis": "分析内容",
                "keyFindings": ["关键发现1", "关键发现2"],
                "businessSuggestions": ["建议1", "建议2"],
                "potentialRisks": ["风险1", "风险2"]
            }
            """;

    @Autowired
    public SqlResultAnalysisAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                .name("数据分析智能体")
                .model(qwenAgentChatModel)
                .description("数据分析智能体")
                .sysPrompt(QUERY_ANALYSIS_PROMPT);

        this.agentScopeAgent = AgentScopeAgent.fromBuilder(builder)
                .name("数据分析智能体")
                .description("数据分析智能体")
                .outputKey("analysisResult")
                .build();
    }

    public Node asNode(boolean includeContents, boolean returnReasoningContents){
        return agentScopeAgent.asNode(includeContents, returnReasoningContents);
    }

}
