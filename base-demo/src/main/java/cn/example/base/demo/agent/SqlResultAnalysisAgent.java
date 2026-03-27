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
import static cn.example.base.demo.enums.SqlQueryNodeEnum.ANALYSIS_NODE;

@Slf4j
@Component
public class SqlResultAnalysisAgent {

    @Getter
    private AgentScopeAgent agentScopeAgent;

    private static final String QUERY_ANALYSIS_PROMPT = """
          你是一个专业的数据分析智能体，专门分析SQL查询结果。

          你的能力：
          1. 理解各种业务数据（销售、商品、客户、库存等）
          2. 识别数据中的模式、趋势和异常
          3. 提供 actionable 的业务建议
          4. 用清晰、结构化的方式呈现分析结果

          分析框架：
          1. 数据概览：总结数据的基本情况
          2. 关键洞察：发现最重要的3-5个点
          3. 深入分析：对重要发现进行详细解释
          4. 业务建议：基于数据提出具体建议
          5. 风险提示：指出潜在问题和风险

          请确保分析：
          - 基于实际数据，避免主观臆断
          - 关注业务价值，而不仅仅是统计
          - 考虑上下文和业务场景
          - 建议要具体、可执行
          """;

    @Autowired
    public SqlResultAnalysisAgent(@Qualifier("qwenAgentChatModel") Model qwenAgentChatModel) {
        ReActAgent.Builder builder = ReActAgent.builder()
                .name(ANALYSIS_NODE.getId())  // 只能是节点名称
                .model(qwenAgentChatModel)
                .description("数据分析智能体")
                .sysPrompt(QUERY_ANALYSIS_PROMPT);

        this.agentScopeAgent = AgentScopeAgent.fromBuilder(builder)
                .name(ANALYSIS_NODE.getId())  // 只能是节点名称
                .description("数据分析智能体")
                .instruction("""
                        # 数据分析任务
                        
                        ## 查询背景
                        - 查询类型：{{queryType}}
                        - 用户查询：{{naturalLanguageQuery}}
                        - 执行SQL：{{generatedSql}}
                        - 数据行数：{{rowCount}} 行
                        - 执行耗时：{{executionTime}} 毫秒
            
                        ## 查询结果数据
                        {{dataJson}}
            
                        ## 数据摘要
                        {{dataSummary}}
            
                        ## 分析要求
                        请基于以上查询结果，进行专业的业务数据分析。
            
                        请按以下结构组织分析报告：
                        1. **数据概览**：整体描述数据情况和主要特征
                        2. **关键发现**：列出最重要的3-5个数据洞察
                        3. **趋势分析**：如果数据包含时间维度，分析变化趋势
                        4. **业务解读**：从业务角度解释数据的意义
                        5. **行动建议**：基于分析结果提出具体建议
                        6. **风险提示**：指出需要注意的风险或问题
            
                        ## 输出格式
                        请输出JSON格式：
                        {
                            "success": true,
                            "dataOverview": "数据概览描述",
                            "keyFindings": ["发现1", "发现2", "发现3"],
                            "trendAnalysis": "趋势分析内容",
                            "businessInsights": "业务解读内容",
                            "actionableSuggestions": ["建议1", "建议2"],
                            "riskWarnings": ["风险1", "风险2"],
                            "confidenceLevel": "HIGH/MEDIUM/LOW"
                        }
                        """)
                .outputKey("analysisResult")
                .build();
    }

    public Node asNode(boolean includeContents, boolean returnReasoningContents){
        return agentScopeAgent.asNode(includeContents, returnReasoningContents);
    }

}
