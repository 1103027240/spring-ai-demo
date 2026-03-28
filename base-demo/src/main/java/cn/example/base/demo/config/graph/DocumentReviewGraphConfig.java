package cn.example.base.demo.config.graph;

import cn.example.base.demo.build.GraphBuild;
import cn.example.base.demo.factory.DocumentReviewFactory;
import cn.example.base.demo.node.document.*;
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
import static cn.example.base.demo.constant.WorkflowConstant.DOCUMENT_REVIEW_NAME;
import static cn.example.base.demo.constant.WorkflowConstant.DOCUMENT_REVIEW_TITLE;
import static cn.example.base.demo.enums.ApprovalDecisionEnum.APPROVE;
import static cn.example.base.demo.enums.ApprovalDecisionEnum.REJECT;
import static cn.example.base.demo.enums.DocumentReviewNodeEnum.*;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async;

/**
 * 文档审批工作流
 * @author 11030
 */
@Configuration
@Slf4j
public class DocumentReviewGraphConfig {

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

        log.info("\n{}", "=".repeat(80));
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
        CompileConfig compileConfig = graphBuild.buildCompileConfig(mySqlSaver, true, HUMAN_APPROVAL.getText());

        log.info("\n{}", "=".repeat(80));
        log.info("=== Document Review Graph ===");
        log.info("工作流名称：{}", DOCUMENT_REVIEW_NAME);
        log.info("标题：{}", DOCUMENT_REVIEW_TITLE);
        log.info("节点列表：");
        log.info("  1. {} -> {}", StateGraph.START, CONTENT_ANALYSIS.getText());
        log.info("  2. {} -> {}", CONTENT_ANALYSIS.getText(), COMPLIANCE_CHECK.getText());
        log.info("  3. {} -> {}", COMPLIANCE_CHECK.getText(), RISK_ASSESSMENT.getText());
        log.info("  4. {} -> {}", RISK_ASSESSMENT.getText(), HUMAN_APPROVAL.getText());
        log.info("  5. {} -> (条件分支)", HUMAN_APPROVAL.getText());
        log.info("     - {} -> {} -> {}", APPROVE.getId(), APPROVE_PROCESSING.getText(), FINAL_REPORT.getText());
        log.info("     - {} -> {} -> {}", REJECT.getId(), REJECT_PROCESSING.getText(), FINAL_REPORT.getText());
        log.info("  6. {} -> {}", FINAL_REPORT.getText(), StateGraph.END);
        log.info("========================================================\n");
        return stateGraph.compile(compileConfig);
    }

    /**
     * 创建状态图
     */
    private StateGraph createDocumentReviewGraph() throws GraphStateException {
        return new StateGraph(DOCUMENT_REVIEW_NAME, DocumentReviewFactory.documentReviewKeyStrategyFactory())
            // 创建节点
            .addNode(CONTENT_ANALYSIS.getText(), node_async(new ContentAnalysisNode()))
            .addNode(COMPLIANCE_CHECK.getText(), node_async(new ComplianceCheckNode()))
            .addNode(RISK_ASSESSMENT.getText(), node_async(new RiskAssessmentNode()))
            .addNode(HUMAN_APPROVAL.getText(), node_async(new HumanApprovalNode()))
            .addNode(APPROVE_PROCESSING.getText(), node_async(new ApproveProcessingNode()))
            .addNode(REJECT_PROCESSING.getText(), node_async(new RejectProcessingNode()))
            .addNode(FINAL_REPORT.getText(), node_async(new FinalReportNode()))

            // 创建边
            .addEdge(StateGraph.START, CONTENT_ANALYSIS.getText())
            .addEdge(CONTENT_ANALYSIS.getText(), COMPLIANCE_CHECK.getText())
            .addEdge(COMPLIANCE_CHECK.getText(), RISK_ASSESSMENT.getText())
            .addEdge(RISK_ASSESSMENT.getText(), HUMAN_APPROVAL.getText())

            // 创建条件边
            .addConditionalEdges(HUMAN_APPROVAL.getText(),
                    edge_async(new ApprovalDecisionRouter()),
                    // 根据条件值找到对应节点
                    Map.of(
                            APPROVE.getId(), APPROVE_PROCESSING.getText(),  //通过
                            REJECT.getId(), REJECT_PROCESSING.getText()  //拒绝
                    ))

            .addEdge(APPROVE_PROCESSING.getText(), FINAL_REPORT.getText())
            .addEdge(REJECT_PROCESSING.getText(), FINAL_REPORT.getText())
            .addEdge(FINAL_REPORT.getText(), StateGraph.END);
    }

}
