package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.MultiAgentBuild;
import cn.example.base.demo.build.CodeSearchRoutingBuild;
import cn.example.base.demo.build.StudioBuild;
import cn.example.base.demo.service.MultiRoutingService;
import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.node.RoutingMergeNode;
import io.agentscope.core.studio.StudioManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;

@Slf4j
@Service
public class MultiRoutingServiceImpl implements MultiRoutingService {

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Resource(name = "codeSearchRoutingAgent")
    private AgentScopeRoutingAgent codeSearchRoutingAgent;

    @Resource(name = "codeSearchGraph")
    private CompiledGraph codeSearchGraph;

    @Autowired
    private CodeSearchRoutingBuild codeSearchRoutingBuild;

    @Autowired
    private StudioBuild studioBuild;

    @Override
    public Map<String, Object> doChatSimple(String message) {
        try {
            studioBuild.initStudio("代码搜索多智能体");

            // 调用大模型
            Map<String, Object> inputs = Map.of(QUERY, message);
            OverAllState overAllState = codeSearchRoutingAgent.invoke(inputs).orElse(null);

            // 封装返回结果
            Object mergedResult = overAllState.value(RoutingMergeNode.DEFAULT_MERGED_OUTPUT_KEY).orElse(null);
            String result = codeSearchRoutingBuild.extractText(mergedResult, message, overAllState);
            return Map.of(SUCCESS, true, DATA, result);
        } catch (Exception e) {
            log.error("【代码搜索多智能体】执行失败", e);
            return Map.of(SUCCESS, false, ERROR, e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

    @Override
    public Map<String, Object> doChatGraph(String message) {
        try {
            studioBuild.initStudio("代码搜索多智能体（工作流）");

            Map<String, Object> inputs = Map.of(QUERY, message);
            OverAllState overAllState = codeSearchGraph.invoke(inputs).orElse(null);

            Object finalResult = overAllState.value(FINAL_RESULT).orElse(null);
            String result = multiAgentBuild.extractText(finalResult);
            return Map.of(SUCCESS, true, DATA, result);
        } catch (Exception e){
            log.error("【代码搜索多智能体（工作流）】执行失败", e);
            return Map.of(SUCCESS, false, ERROR, e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

}
