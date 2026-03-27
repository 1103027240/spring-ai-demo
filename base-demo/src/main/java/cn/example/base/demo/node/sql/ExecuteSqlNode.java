package cn.example.base.demo.node.sql;

import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import cn.example.base.demo.tools.QueryTools;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;

/**
 * 4、SQL执行节点
 */
@Slf4j
@Component
public class ExecuteSqlNode implements NodeAction {

    @Autowired
    private QueryTools queryTools;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】SQL执行节点开始执行");

        try {
            String generatedSql = state.value(GENERATED_SQL, String.class).orElse("");
            if (StrUtil.isBlank(generatedSql)) {
                return Map.of(
                        ERROR, "SQL语句为空",
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            // 调用QueryTools.executeSql
            Map<String, Object> executeSqlResult = queryTools.executeSql(generatedSql);

            boolean success = (boolean) executeSqlResult.getOrDefault(SUCCESS, false);
            if (!success) {
                return Map.of(
                        EXECUTE_SQL_RESULT, executeSqlResult,
                        ERROR, "SQL执行失败: " + executeSqlResult.get(ERROR),
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            return Map.of(
                    EXECUTE_SQL_RESULT, executeSqlResult,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.PROCESSING.getId(),
                    CURRENT_NODE, EXECUTE_SQL_NODE.getId(),
                    NEXT_NODE, ANALYSIS_NODE.getId(),
                    SUCCESS, true);
        } catch (Exception e) {
            log.error("【数据查询智能体】SQL执行节点执行失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getId());
        }
    }

}
