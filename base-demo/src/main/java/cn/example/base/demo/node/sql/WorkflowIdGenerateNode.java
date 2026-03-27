package cn.example.base.demo.node.sql;

import cn.example.base.demo.build.WorkflowBuild;
import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
        log.info("【数据查询智能体】流程ID生成节点开始执行");

        try {
            String naturalLanguageQuery = state.value(QUERY, String.class).orElse("");
            String workflowId = state.value(WORKFLOW_ID, String.class).orElse(WorkflowBuild.generateWorkflowId());

            return Map.of(
                    WORKFLOW_ID, workflowId,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.STARTED.getId(),
                    NATURAL_LANGUAGE_QUERY, naturalLanguageQuery,
                    CURRENT_NODE, WORKFLOW_ID_GENERATE_NODE.getId(),
                    NEXT_NODE, NL_TO_SQL_NODE.getId());
        } catch (Exception e) {
            log.error("【数据查询智能体】流程ID生成节点执行失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getId());
        }
    }

}
