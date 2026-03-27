package cn.example.base.demo.node.sql;

import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.ERROR_HANDLE_NODE;

/**
 * 错误处理节点
 */
@Slf4j
@Component
public class ErrorHandleNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】错误处理节点开始执行");

        try {
            String error = state.value(ERROR, String.class).orElse("未知错误");
            String naturalLanguageQuery = state.value(NATURAL_LANGUAGE_QUERY, String.class).orElse("");

            // 构建错误结果
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put(WORKFLOW_ID, state.value(WORKFLOW_ID, String.class).orElse(""));
            errorResult.put(NATURAL_LANGUAGE_QUERY, naturalLanguageQuery);
            errorResult.put(SUGGESTION, "请重新表述查询或联系管理员");
            errorResult.put(ERROR, error);
            errorResult.put(WORKFLOW_STATUS, QueryWorkflowStatusEnum.FAILED.getId());
            errorResult.put(SUCCESS, false);

            return Map.of(
                    FINAL_RESULT, errorResult,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.FAILED.getId(),
                    CURRENT_NODE, ERROR_HANDLE_NODE.getId(),
                    NEXT_NODE, StateGraph.END,
                    SUCCESS, false);
        } catch (Exception e) {
            log.error("【数据查询智能体】错误处理节点执行失败", e);
            return Map.of(
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.CRITICAL_ERROR.getId(),
                    NEXT_NODE, StateGraph.END);
        }
    }

}
