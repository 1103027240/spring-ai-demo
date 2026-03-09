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
                .addNode("内容分析", node_async(new ContentAnalysisNode()))
                .addNode("合规检查", node_async(new ComplianceCheckNode()))
                .addNode("风险评估", node_async(new RiskAssessmentNode()))
                .addNode("人工审批", node_async(new HumanApprovalNode()))
                .addNode("通过", node_async(new ApproveProcessingNode()))
                .addNode("拒绝", node_async(new RejectProcessingNode()))
                .addNode("最终报告", node_async(new FinalReportNode()))

                // 创建边
                .addEdge(StateGraph.START, "内容分析")
                .addEdge("内容分析", "合规检查")
                .addEdge("合规检查", "风险评估")
                .addEdge("风险评估", "人工审批")

                // 创建条件边
                .addConditionalEdges("人工审批",
                        edge_async(new ApprovalDecisionRouter()),
                        Map.of(
                                "APPROVE", "通过", // 通过节点
                                "REJECT", "拒绝" //拒绝节点
                        ))

                .addEdge("通过", "最终报告")
                .addEdge("拒绝", "最终报告")
                .addEdge("最终报告", StateGraph.END);

        // 配置持久化和中断节点
        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(mySqlSaver) // 注册Mysql状态存储
                        .build())
                .interruptBefore("人工审批") // 在此节点前中断
                .build();

        // 配置可视化视图
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "DocumentReviewGraph");
        log.info("\n=== Document Review UML Flow ===");
        log.info(representation.content());
        log.info("==========================\n");

        return stateGraph.compile(compileConfig);
    }

}
