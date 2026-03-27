package cn.example.base.demo.node.sql;

import cn.example.base.demo.build.WorkflowBuild;
import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;

/**
 * 1、流程ID生成节点
 */
@Slf4j
@Component
public class WorkflowIdGenerateNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("【数据查询智能体】开始节点执行");

        try {
            Map<String, Object> queryRequest = state.value(QUERY_REQUEST, Map.class).orElse(new HashMap<>());
            String naturalLanguageQuery = (String) queryRequest.getOrDefault(QUERY, "");
            String workflowId = state.value(WORKFLOW_ID, String.class).orElse(WorkflowBuild.generateWorkflowId());

            return Map.of(
                    WORKFLOW_ID, workflowId,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.STARTED.getId(),
                    NATURAL_LANGUAGE_QUERY, naturalLanguageQuery,
                    CURRENT_NODE, WORKFLOW_ID_GENERATE_NODE.getId(),
                    NEXT_NODE, NL_TO_SQL_NODE.getId());
        } catch (Exception e) {
            log.error("工作流开始节点失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getId());
        }
    }

}
