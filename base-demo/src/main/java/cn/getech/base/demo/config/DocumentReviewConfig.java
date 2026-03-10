package cn.getech.base.demo.config;

import cn.getech.base.demo.contant.WorkFlowTitleConstant;
import cn.getech.base.demo.enums.ApprovalDecisionEnum;
import cn.getech.base.demo.factory.DocumentReviewFactory;
import cn.getech.base.demo.node.document.*;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.util.Map;
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

    /**
     * 状态存储持久化方式: mysql
     */
    @Bean
    public MysqlSaver mySqlSaver(DataSource dataSource) {
        return MysqlSaver.builder()
                .dataSource(dataSource) // 注入数据源
                .createOption(CreateOption.CREATE_IF_NOT_EXISTS) // 表不存在则自动创建
                .build(); // 使用默认StateSerializer（先转成二进制，然后再base64加密，序列化方法encodeState，反序列化方法decodeState）
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
        // 创建状态图
        StateGraph stateGraph = new StateGraph(WorkFlowTitleConstant.DOCUMENT_REVIEW_TITLE, DocumentReviewFactory.documentReviewKeyStrategyFactory())
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
                        Map.of(
                                APPROVE.getId(), APPROVE_PROCESSING.getName(), // 通过节点
                                REJECT.getId(), REJECT_PROCESSING.getName() //拒绝节点
                        ))

                .addEdge(APPROVE_PROCESSING.getName(), FINAL_REPORT.getName())
                .addEdge(REJECT_PROCESSING.getName(), FINAL_REPORT.getName())
                .addEdge(FINAL_REPORT.getName(), StateGraph.END);

        // 配置持久化和中断节点
        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(mySqlSaver) // Mysql状态存储
                        .build())
                .interruptBefore(HUMAN_APPROVAL.getName()) // 在此节点前中断
                .build();

        // 配置可视化视图
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, WorkFlowTitleConstant.DOCUMENT_REVIEW_TITLE);
        log.info("\n=== Document Review UML Flow ===");
        log.info(representation.content());
        log.info("==========================\n");

        return stateGraph.compile(compileConfig);
    }

}
