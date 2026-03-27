package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.WorkflowBuild;
import cn.example.base.demo.service.SqlQueryWorkflowService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import io.agentscope.core.studio.StudioManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.constant.FieldConstant.THREAD_ID;

@Service
public class SqlQueryWorkflowServiceImpl implements SqlQueryWorkflowService {

    @Resource(name = "sqlQueryGraph")
    private CompiledGraph sqlQueryGraph;

    @Override
    public Map<String, Object> executeWorkflow(String message) {

        try {
            StudioManager.init()
                    .studioUrl("http://localhost:3000")
                    .project("顺序多智能体")
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

            return sqlQueryGraph.invoke(overAllState, config)
                    .map(OverAllState::data)
                    .orElse(null);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            StudioManager.shutdown();
        }
    }

}
