package cn.example.agent.demo.config.graph;

import cn.example.agent.demo.build.GraphBuild;
import cn.example.agent.demo.factory.CodeSearchFactory;
import cn.example.agent.demo.node.routing.PostProcessNode;
import cn.example.agent.demo.node.routing.PreProcessNode;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static cn.example.agent.demo.constant.WorkflowConstant.CODE_SEARCH_NAME;
import static cn.example.agent.demo.constant.WorkflowConstant.CODE_SEARCH_TITLE;
import static cn.example.agent.demo.enums.CodeSearchNodeEnum.*;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Slf4j
@Configuration
public class CodeSearchGraphConfig {

    @Autowired
    private GraphBuild graphBuild;

    @Autowired
    private PreProcessNode preProcessNode;

    @Autowired
    private PostProcessNode postProcessNode;

    @Resource(name = "codeSearchRoutingAgent")
    private LlmRoutingAgent codeSearchRoutingAgent;

    public StateGraph createCodeSearchGraph() throws GraphStateException {
        StateGraph graph = new StateGraph(CODE_SEARCH_NAME, CodeSearchFactory.codeSearchKeyStrategyFactory());

        graph.addNode(PRE_PROCESS_NODE.getText(), node_async(preProcessNode))
                .addNode(ROUTING_NODE.getText(), codeSearchRoutingAgent.getAndCompileGraph())
                .addNode(POST_PROCESS_NODE.getText(), node_async(postProcessNode))
                .addEdge(StateGraph.START, PRE_PROCESS_NODE.getText())
                .addEdge(PRE_PROCESS_NODE.getText(), ROUTING_NODE.getText())
                .addEdge(ROUTING_NODE.getText(), POST_PROCESS_NODE.getText())
                .addEdge(POST_PROCESS_NODE.getText(), StateGraph.END);

        return graph;
    }

    @Bean
    public CompiledGraph codeSearchGraph(MysqlSaver mySqlSaver) throws GraphStateException {
        StateGraph stateGraph = createCodeSearchGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(stateGraph, CODE_SEARCH_TITLE);

        // 配置持久化
        CompileConfig compileConfig = graphBuild.buildCompileConfig(mySqlSaver, false, null);

        return stateGraph.compile(compileConfig);
    }

    @Bean
    public GraphRepresentation codeSearchGraphRepresentation() throws GraphStateException {
        StateGraph stateGraph = createCodeSearchGraph();

        // 生成PlantUML格式的可视化图
        GraphRepresentation representation = graphBuild.buildGraphRepresentation(stateGraph, CODE_SEARCH_TITLE);

        log.info("\n{}", "=".repeat(80));
        log.info("=== Code Search Graph ===");
        log.info(representation.content());
        log.info("========================================================\n");
        return representation;
    }

}
