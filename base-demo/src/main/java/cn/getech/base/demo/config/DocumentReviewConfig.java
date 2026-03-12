package cn.getech.base.demo.config;

import cn.getech.base.demo.build.GraphBuild;
import cn.getech.base.demo.factory.DocumentReviewFactory;
import cn.getech.base.demo.node.document.*;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;
import static cn.getech.base.demo.constant.WorkflowConstant.DOCUMENT_REVIEW_NAME;
import static cn.getech.base.demo.constant.WorkflowConstant.DOCUMENT_REVIEW_TITLE;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.APPROVE;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.REJECT;
import static cn.getech.base.demo.enums.DocumentReviewNodeEnum.*;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async;

/**
 * 文档审批工作流
 * @author 11030
 */
@Configuration
@Slf4j
public class DocumentReviewConfig {

    @Autowired
    private GraphBuild graphBuild;

    /**
     * 工作流Graph可视化表示，用于Studio展示工作流结构
     */
    @Bean
    public GraphRepresentation documentReviewGraphRepresentation() throws GraphStateException {
        StateGraph stateGraph = createDocumentReviewGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(stateGraph, DOCUMENT_REVIEW_TITLE);

        log.info("\n" + "=".repeat(80));
        log.info("=== Document Review Graph ===");
        log.info(representation.content());
        log.info("========================================================\n");

        return representation;
    }

    /**
     * 工作流
     * 1.内容分析
     * 2.合规检查
     * 3.风险评估
     * 4.人工审批（中断点）
     * 5.根据审批结果分支处理
     * 6.最终报告
     */
    @Bean
    public CompiledGraph documentReviewGraph(MysqlSaver mySqlSaver) throws GraphStateException {
        StateGraph stateGraph = createDocumentReviewGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(stateGraph, DOCUMENT_REVIEW_TITLE);

        // 配置持久化和中断节点
        CompileConfig compileConfig = graphBuild.buildCompileConfig(mySqlSaver, true, HUMAN_APPROVAL.getName());

        log.info("\n" + "=".repeat(80));
        log.info("=== Document Review Graph ===");
        log.info("工作流名称：{}", DOCUMENT_REVIEW_NAME);
        log.info("标题：{}", DOCUMENT_REVIEW_TITLE);
        log.info("节点列表：");
        log.info("  1. {} -> {}", StateGraph.START, CONTENT_ANALYSIS.getName());
        log.info("  2. {} -> {}", CONTENT_ANALYSIS.getName(), COMPLIANCE_CHECK.getName());
        log.info("  3. {} -> {}", COMPLIANCE_CHECK.getName(), RISK_ASSESSMENT.getName());
        log.info("  4. {} -> {}", RISK_ASSESSMENT.getName(), HUMAN_APPROVAL.getName());
        log.info("  5. {} -> (条件分支)", HUMAN_APPROVAL.getName());
        log.info("     - {} -> {} -> {}", APPROVE.getId(), APPROVE_PROCESSING.getName(), FINAL_REPORT.getName());
        log.info("     - {} -> {} -> {}", REJECT.getId(), REJECT_PROCESSING.getName(), FINAL_REPORT.getName());
        log.info("  6. {} -> {}", FINAL_REPORT.getName(), StateGraph.END);
        log.info("========================================================\n");

        return stateGraph.compile(compileConfig);
    }

    /**
     * 创建状态图
     */
    private StateGraph createDocumentReviewGraph() throws GraphStateException {
        return new StateGraph(DOCUMENT_REVIEW_NAME, DocumentReviewFactory.documentReviewKeyStrategyFactory())
            // 创建节点
            .addNode(CONTENT_ANALYSIS.getName(), node_async(new ContentAnalysisNode()))
            .addNode(COMPLIANCE_CHECK.getName(), node_async(new ComplianceCheckNode()))
            .addNode(RISK_ASSESSMENT.getName(), node_async(new RiskAssessmentNode()))
            .addNode(HUMAN_APPROVAL.getName(), node_async(new HumanApprovalNode()))
            .addNode(APPROVE_PROCESSING.getName(), node_async(new ApproveProcessingNode()))
            .addNode(REJECT_PROCESSING.getName(), node_async(new RejectProcessingNode()))
            .addNode(FINAL_REPORT.getName(), node_async(new FinalReportNode()))

            // 创建边
            .addEdge(StateGraph.START, CONTENT_ANALYSIS.getName())
            .addEdge(CONTENT_ANALYSIS.getName(), COMPLIANCE_CHECK.getName())
            .addEdge(COMPLIANCE_CHECK.getName(), RISK_ASSESSMENT.getName())
            .addEdge(RISK_ASSESSMENT.getName(), HUMAN_APPROVAL.getName())

            // 创建条件边
            .addConditionalEdges(HUMAN_APPROVAL.getName(),
                    edge_async(new ApprovalDecisionRouter()),
                    // 根据条件值（APPROVE/REJECT）找到对应节点
                    Map.of(
                            APPROVE.getId(), APPROVE_PROCESSING.getName(),
                            REJECT.getId(), REJECT_PROCESSING.getName()
                    ))

            .addEdge(APPROVE_PROCESSING.getName(), FINAL_REPORT.getName())
            .addEdge(REJECT_PROCESSING.getName(), FINAL_REPORT.getName())
            .addEdge(FINAL_REPORT.getName(), StateGraph.END);
    }

}
