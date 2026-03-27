package cn.example.base.demo.config;

import cn.example.base.demo.agent.SqlResultAnalysisAgent;
import cn.example.base.demo.build.GraphBuild;
import cn.example.base.demo.factory.QueryWorkflowFactory;
import cn.example.base.demo.node.sql.*;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.*;
import static cn.example.base.demo.constant.WorkflowConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Slf4j
@Configuration
public class SqlQueryWorkflowConfig {

    @Autowired
    private SqlResultAnalysisAgent sqlResultAnalysisAgent;

    @Autowired
    private GraphBuild graphBuild;

    @Autowired
    private WorkflowIdGenerateNode workflowIdGenerateNode;

    @Autowired
    private NlToSqlNode nlToSqlNode;

    @Autowired
    private ValidateSqlNode validateSqlNode;

    @Autowired
    private ExecuteSqlNode executeSqlNode;

    @Autowired
    private SqlResultGenerateNode sqlResultGenerateNode;

    @Autowired
    private ErrorHandleNode errorHandleNode;

    public StateGraph createSqlQueryGraph() throws GraphStateException {
        StateGraph graph = new StateGraph(SQL_QUERY_NAME, QueryWorkflowFactory.queryKeyStrategyFactory());

        // 1、流程ID生成节点
        graph.addNode(WORKFLOW_ID_GENERATE_NODE.getId(), node_async(workflowIdGenerateNode));

        // 2、自然语言查询转SQL节点
        graph.addNode(NL_TO_SQL_NODE.getId(), node_async(nlToSqlNode));

        // 3、SQL验证节点
        graph.addNode(VALIDATE_SQL_NODE.getId(), node_async(validateSqlNode));

        // 4、SQL执行节点
        graph.addNode(EXECUTE_SQL_NODE.getId(), node_async(executeSqlNode));

        // 5、数据分析节点
        graph.addNode(ANALYSIS_NODE.getId(), sqlResultAnalysisAgent.asNode(true, false));

        // 6、查询结果生成节点
        graph.addNode(QUERY_RESULT_GENERATE.getId(), node_async(sqlResultGenerateNode));

        // 7、错误处理节点
        graph.addNode(ERROR_HANDLE_NODE.getId(), node_async(errorHandleNode));

        // 开始节点 -> 流程ID生成节点
        graph.addEdge(StateGraph.START, WORKFLOW_ID_GENERATE_NODE.getId());

        // 流程ID生成节点 -> 自然语言查询转SQL节点
        graph.addEdge(WORKFLOW_ID_GENERATE_NODE.getId(), NL_TO_SQL_NODE.getId());

        // 自然语言查询转SQL节点 -> SQL验证节点
        graph.addConditionalEdges(NL_TO_SQL_NODE.getId(), edge_async(new NlToSqlNodeCondition()),
            Map.of(
                    VALIDATE_SQL_NODE.getId(), VALIDATE_SQL_NODE.getId(),
                    ERROR_HANDLE_NODE.getId(), ERROR_HANDLE_NODE.getId()));

        // SQL验证节点 -> SQL执行节点
        graph.addConditionalEdges(VALIDATE_SQL_NODE.getId(), edge_async(new ValidateSqlNodeCondition()),
            Map.of(
                    EXECUTE_SQL_NODE.getId(), EXECUTE_SQL_NODE.getId(),
                    ERROR_HANDLE_NODE.getId(), ERROR_HANDLE_NODE.getId()));

        // SQL执行节点 -> 数据分析节点
        graph.addConditionalEdges(EXECUTE_SQL_NODE.getId(), edge_async(new ExecuteSqlNodeCondition()),
            Map.of(
                    ANALYSIS_NODE.getId(), ANALYSIS_NODE.getId(),
                    ERROR_HANDLE_NODE.getId(), ERROR_HANDLE_NODE.getId()));

        // 数据分析节点 -> 结果生成节点
        graph.addConditionalEdges(ANALYSIS_NODE.getId(), edge_async(new SqlResultAnalysisNodeCondition()),
            Map.of(
                    QUERY_RESULT_GENERATE.getId(), QUERY_RESULT_GENERATE.getId(),
                    ERROR_HANDLE_NODE.getId(), ERROR_HANDLE_NODE.getId()));

        // 结果生成节点 -> 结束
        graph.addEdge(QUERY_RESULT_GENERATE.getId(), StateGraph.END);

        // 错误处理 -> 结束
        graph.addEdge(ERROR_HANDLE_NODE.getId(), StateGraph.END);

        return graph;
    }

    @Bean
    public CompiledGraph sqlQueryGraph(MysqlSaver mySqlSaver) throws GraphStateException {
        StateGraph sqlQueryGraph = createSqlQueryGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(sqlQueryGraph, SQL_QUERY_TITLE);

        // 配置持久化
        CompileConfig compileConfig = graphBuild.buildCompileConfig(mySqlSaver, false, null);

        return sqlQueryGraph.compile(compileConfig);
    }

}
