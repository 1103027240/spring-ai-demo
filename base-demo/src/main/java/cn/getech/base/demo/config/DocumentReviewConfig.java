package cn.getech.base.demo.config;

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
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async;

/**
 * 文档审批工作流
 * @author 11030
 */
@Configuration
@Slf4j
public class DocumentReviewConfig {

    @Bean
    public MysqlSaver mySqlSaver(DataSource dataSource) {
        return MysqlSaver.builder()
                .dataSource(dataSource) // 注入数据源
                .createOption(CreateOption.CREATE_IF_NOT_EXISTS) // 表不存在则自动创建
                .build(); // 使用默认的 StateSerializer（先转成二进制，然后再base64加密，序列化方法encodeState，反序列化方法decodeState）
    }

    /**
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
        StateGraph stateGraph = new StateGraph("DocumentReviewGraph", DocumentReviewFactory.documentReviewKeyStrategyFactory())
                // 创建节点
                .addNode("content_analysis", node_async(new ContentAnalysisNode()))
                .addNode("compliance_check", node_async(new ComplianceCheckNode()))
                .addNode("risk_assessment", node_async(new RiskAssessmentNode()))
                .addNode("human_approval", node_async(new HumanApprovalNode()))
                .addNode("approve_processing", node_async(new ApproveProcessingNode()))
                .addNode("reject_processing", node_async(new RejectProcessingNode()))
                .addNode("final_report", node_async(new FinalReportNode()))

                // 创建边
                .addEdge(StateGraph.START, "content_analysis")
                .addEdge("content_analysis", "compliance_check")
                .addEdge("compliance_check", "risk_assessment")
                .addEdge("risk_assessment", "human_approval")

                // 创建条件边
                .addConditionalEdges("human_approval",
                        edge_async(new ApprovalDecisionRouter()),
                        Map.of(
                                "APPROVE", "approve_processing",
                                "REJECT", "reject_processing"
                        ))

                .addEdge("approve_processing", "final_report")
                .addEdge("reject_processing", "final_report")
                .addEdge("final_report", StateGraph.END);

        // 配置持久化和中断节点
        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(mySqlSaver) // 注册Mysql状态存储
                        .build())
                .interruptBefore("human_approval") // 关键：在此节点前中断
                .build();

        // 配置可视化视图
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "DocumentReviewGraph");
        log.info("\n=== Document Review UML Flow ===");
        log.info(representation.content());
        log.info("==========================\n");

        return stateGraph.compile(compileConfig);
    }

}
