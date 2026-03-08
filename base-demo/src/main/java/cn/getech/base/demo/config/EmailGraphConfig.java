package cn.getech.base.demo.config;

import cn.getech.base.demo.factory.CustomEmailKeyStrategyFactory;
import cn.getech.base.demo.node.email.*;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 邮件处理工作流
 * @author 11030
 */
@Slf4j
@Configuration
public class EmailGraphConfig {

    @Bean
    public CompiledGraph emailGraph() throws GraphStateException {
        // 创建节点
        var readEmail = node_async(new ReadEmailNode());
        var classifyIntent = node_async(new ClassifyIntentNode());
        var searchDocument = node_async(new SearchDocumentNode());
        var bugTrack = node_async(new BugTrackNode());
        var draftResponse = node_async(new DraftResponseNode());
        var humanReview = node_async(new HumanReviewNode());
        var sendReply = node_async(new SendReplyNode());

        // 创建状态图
        StateGraph stateGraph = new StateGraph("EmailGraph", CustomEmailKeyStrategyFactory.emailKeyStrategyFactory())
                .addNode("read_email", readEmail)
                .addNode("classify_intent", classifyIntent)
                .addNode("search_document", searchDocument)
                .addNode("bug_track", bugTrack)
                .addNode("draft_response", draftResponse)
                .addNode("human_review", humanReview)
                .addNode("send_reply", sendReply);

        // 创建边
        stateGraph.addEdge(StateGraph.START, "read_email")
                .addEdge("read_email", "classify_intent")
                .addEdge("send_reply", StateGraph.END);

        // 创建条件边
        stateGraph.addConditionalEdges(
                "classify_intent",
                edge_async(state -> state.value("nextNode", "draft_response")), //获取nextNode值
                Map.of(
                        "search_document", "search_document",
                        "bug_track", "bug_track",
                        "human_review", "human_review",
                        "draft_response", "draft_response"));

        stateGraph.addConditionalEdges("human_review",
                edge_async(state -> state.value("next_node", "send_reply")),
                Map.of(
                        "send_reply", "send_reply"
                ));

        stateGraph.addConditionalEdges("draft_response",
                edge_async(state -> state.value("next_node", "send_reply")),
                Map.of(
                        "human_review", "human_review",
                        "send_reply", "send_reply"
                ));

        stateGraph.addEdge("search_document", "draft_response");
        stateGraph.addEdge("bug_track", "draft_response");

        // 配置持久化
        var memory = new MemorySaver();
        var compileConfig= CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(memory)
                        .build())
                .interruptBefore("human_review")  // 在人工审核前中断
                .build();

        // 配置可视化视图
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "EmailGraph");
        log.info("\n=== expander Email UML Flow ===");
        log.info(representation.content());
        log.info("==========================\n");

        return stateGraph.compile(compileConfig);
    }

}
