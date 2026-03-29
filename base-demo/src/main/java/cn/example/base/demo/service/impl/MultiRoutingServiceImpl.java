package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.MultiAgentBuild;
import cn.example.base.demo.build.CodeSearchRoutingBuild;
import cn.example.base.demo.service.MultiRoutingService;
import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.node.RoutingMergeNode;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.agentscope.core.studio.StudioManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;

@Slf4j
@Service
public class MultiRoutingServiceImpl implements MultiRoutingService {

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Autowired
    private CodeSearchRoutingBuild codeSearchRoutingBuild;

    @Resource(name = "codeSearchRoutingAgent")
    private AgentScopeRoutingAgent codeSearchRoutingAgent;

    @Resource(name = "codeSearchGraph")
    private CompiledGraph codeSearchGraph;

    @Override
    public Map<String, Object> doChatSimple(String message) {
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("代码搜索多智能体")
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();

        try {
            // 调用大模型
            Map<String, Object> inputs = Map.of(QUERY, message);
            OverAllState state = codeSearchRoutingAgent.invoke(inputs).orElse(null);
            Object mergedResult = state.value(RoutingMergeNode.DEFAULT_MERGED_OUTPUT_KEY).orElse(null);

            // 封装返回结果
            String result;
            if (mergedResult != null) {
                result = multiAgentBuild.extractText(mergedResult);
            } else {
                result = synthesize(message, state);
            }

            return Map.of(SUCCESS, true, DATA, result);
        } catch (GraphRunnerException e) {
            log.error("【代码搜索】执行失败", e);
            return Map.of(SUCCESS, false, ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> doChatGraph(String message) {
        Map<String, Object> inputs = Map.of(QUERY, message);
        OverAllState overAllState = codeSearchGraph.invoke(inputs).orElse(null);

        Object value = overAllState.value(FINAL_RESULT).orElse(null);
        return Map.of(SUCCESS, true, DATA, multiAgentBuild.extractText(value));
    }

    public String synthesize(String query, OverAllState overAllState) {
        List<String> resultTexts = new ArrayList<>();
        String[] agentKeys = {"github_result", "gitee_result", "csdn_result"};

        for (String key : agentKeys) {
            overAllState.value(key).ifPresent(value -> resultTexts.add(multiAgentBuild.extractText(value)));
        }

       return codeSearchRoutingBuild.synthesize(query, resultTexts);
    }

}
