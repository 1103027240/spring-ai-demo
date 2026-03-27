package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.WorkflowBuild;
import cn.example.base.demo.service.SqlQueryWorkflowService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import io.agentscope.core.studio.StudioManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.constant.FieldConstant.THREAD_ID;

@Slf4j
@Service
public class SqlQueryWorkflowServiceImpl implements SqlQueryWorkflowService {

    @Resource(name = "sqlQueryGraph")
    private CompiledGraph sqlQueryGraph;

    @Override
    public Map<String, Object> executeWorkflow(String message) {

        try {
            StudioManager.init()
                    .studioUrl("http://localhost:3000")
                    .project("数据查询多智能体")
                    .runName("run_" + System.currentTimeMillis())
                    .initialize()
                    .block();

            Map<String, Object> initialState = new HashMap<>();
            String workflowId = WorkflowBuild.generateWorkflowId();
            initialState.put(WORKFLOW_ID, workflowId);
            initialState.put(QUERY, message);
            initialState.put(THREAD_ID, workflowId);

            OverAllState overAllState = new OverAllState(initialState);
            RunnableConfig config = RunnableConfig.builder().threadId(workflowId).build();

            Map<String, Object> response = sqlQueryGraph.invoke(overAllState, config)
                    .map(OverAllState::data)
                    .orElse(null);

            return Map.of(SUCCESS, true, FINAL_RESULT, response.get(FINAL_RESULT));
        } catch (RuntimeException e) {
            log.error("数据查询流程执行失败", e);
            return Map.of(SUCCESS, false, MESSAGE, e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

}
