package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.build.CodeSearchRoutingBuild;
import cn.example.agent.demo.build.MultiAgentBuild;
import cn.example.agent.demo.service.MultiRoutingService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import static cn.example.agent.demo.constant.FieldConstant.*;

@Slf4j
@Service
public class MultiRoutingServiceImpl implements MultiRoutingService {

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Resource(name = "codeSearchRoutingAgent")
    private LlmRoutingAgent codeSearchRoutingAgent;

    @Resource(name = "codeSearchGraph")
    private CompiledGraph codeSearchGraph;

    @Autowired
    private CodeSearchRoutingBuild codeSearchRoutingBuild;

    @Override
    public Map<String, Object> doChatSimple(String message) {
        try {
            // 调用大模型
            Map<String, Object> inputs = Map.of(QUERY, message);
            OverAllState overAllState = codeSearchRoutingAgent.invoke(inputs).orElse(null);

            // 封装返回结果
            Object mergedResult = overAllState.value(OUT_RESULT).orElse(null);
            String result = codeSearchRoutingBuild.extractText(mergedResult, message, overAllState);
            return Map.of(SUCCESS, true, DATA, result);
        } catch (Exception e) {
            log.error("【代码搜索多智能体】执行失败", e);
            return Map.of(SUCCESS, false, ERROR, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> doChatGraph(String message) {
        try {
            Map<String, Object> inputs = Map.of(QUERY, message);
            OverAllState overAllState = codeSearchGraph.invoke(inputs).orElse(null);

            Object finalResult = overAllState.value(FINAL_RESULT).orElse(null);
            String result = multiAgentBuild.extractText(finalResult);
            return Map.of(SUCCESS, true, DATA, result);
        } catch (Exception e){
            log.error("【代码搜索多智能体（工作流）】执行失败", e);
            return Map.of(SUCCESS, false, ERROR, e.getMessage());
        }
    }

}
