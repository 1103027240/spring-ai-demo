package cn.example.ai.demo.config.graph;

import cn.example.ai.demo.build.GraphBuild;
import cn.example.ai.demo.factory.DocumentReviewFactory;
import cn.example.ai.demo.node.customer.*;
import cn.example.ai.demo.node.customer.*;
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
import static cn.example.ai.demo.constant.WorkflowConstant.CUSTOMER_SERVICE_NAME;
import static cn.example.ai.demo.constant.WorkflowConstant.CUSTOMER_SERVICE_TITLE;
import static cn.example.ai.demo.enums.CustomerServiceNodeEnum.*;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async;

/**
 * 售后客服工作流
 * @author 11030
 */
@Configuration
@Slf4j
public class CustomerServiceGraphConfig {

    @Autowired
    private GraphBuild graphBuild;

    /**
     * 1. 意图识别
     * 2. 情感分析
     * 3. 条件路由
     * 4. 知识库检索/订单查询/售后处理
     * 5. 回复生成
     */
    public StateGraph createCustomerServiceGraph() throws GraphStateException {
        return new StateGraph(CUSTOMER_SERVICE_NAME, DocumentReviewFactory.documentReviewKeyStrategyFactory())
            // 创建节点
            .addNode(INTENT_RECOGNITION.getText(), node_async(new IntentRecognitionNode()))
            .addNode(SENTIMENT_ANALYSIS.getText(), node_async(new SentimentAnalysisNode()))
            .addNode(KNOWLEDGE_RETRIEVAL.getText(), node_async(new KnowledgeRetrievalNode()))
            .addNode(ORDER_QUERY.getText(), node_async(new OrderQueryNode()))
            .addNode(AFTER_SALES.getText(), node_async(new AfterSalesNode()))
            .addNode(RESPONSE_GENERATION.getText(), node_async(new ResponseGenerationNode()))

            // 创建边
            .addEdge(StateGraph.START, INTENT_RECOGNITION.getText())
            .addEdge(INTENT_RECOGNITION.getText(), SENTIMENT_ANALYSIS.getText())

            // 创建条件边
            .addConditionalEdges(SENTIMENT_ANALYSIS.getText(), edge_async(new CustomerServiceDecisionCondition()),
                    Map.of(
                            ORDER_QUERY.getId(), ORDER_QUERY.getText(),  // 订单查询
                            AFTER_SALES.getId(), AFTER_SALES.getText(),  // 售后处理
                            KNOWLEDGE_RETRIEVAL.getId(), KNOWLEDGE_RETRIEVAL.getText()))  // 知识库检索

            .addEdge(ORDER_QUERY.getText(), RESPONSE_GENERATION.getText())
            .addEdge(AFTER_SALES.getText(), RESPONSE_GENERATION.getText())
            .addEdge(KNOWLEDGE_RETRIEVAL.getText(), RESPONSE_GENERATION.getText())
            .addEdge(RESPONSE_GENERATION.getText(), StateGraph.END);
    }

    @Bean
    public CompiledGraph customerServiceGraph(MysqlSaver mySqlSaver) throws GraphStateException {
        StateGraph stateGraph = createCustomerServiceGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(stateGraph, CUSTOMER_SERVICE_TITLE);

        // 配置持久化
        CompileConfig compileConfig = graphBuild.buildCompileConfig(mySqlSaver, false, null);

        log.info("\n{}", "=".repeat(80));
        log.info("=== Customer Service Graph ===");
        log.info("工作流名称：{}", CUSTOMER_SERVICE_NAME);
        log.info("标题：{}", CUSTOMER_SERVICE_TITLE);
        log.info("节点列表：");
        log.info("  1. {} -> {}", StateGraph.START, INTENT_RECOGNITION.getText());
        log.info("  2. {} -> {}", INTENT_RECOGNITION.getText(), SENTIMENT_ANALYSIS.getText());
        log.info("  3. {} -> (条件分支)", SENTIMENT_ANALYSIS.getText());
        log.info("     - {} -> {} -> {}", ORDER_QUERY.getId(), ORDER_QUERY.getText(), RESPONSE_GENERATION.getText());
        log.info("     - {} -> {} -> {}", AFTER_SALES.getId(), AFTER_SALES.getText(), RESPONSE_GENERATION.getText());
        log.info("     - {} -> {} -> {}", KNOWLEDGE_RETRIEVAL.getId(), KNOWLEDGE_RETRIEVAL.getText(), RESPONSE_GENERATION.getText());
        log.info("  4. {} -> {}", RESPONSE_GENERATION.getText(), StateGraph.END);
        log.info("{}\n", "=".repeat(80));
        return stateGraph.compile(compileConfig);
    }

    /**
     * 工作流Graph可视化表示，用于Studio展示工作流结构
     */
    @Bean
    public GraphRepresentation customerServiceGraphRepresentation() throws GraphStateException {
        StateGraph stateGraph = createCustomerServiceGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(stateGraph, CUSTOMER_SERVICE_TITLE);

        log.info("\n{}", "=".repeat(80));
        log.info("=== Customer Service Graph ===");
        log.info(representation.content());
        log.info("========================================================\n");
        return representation;
    }

}
