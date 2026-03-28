package cn.example.base.demo.node.sql;

import cn.example.base.demo.enums.SqlQueryWorkflowStatusEnum;
import cn.example.base.demo.tools.SqlQueryTools;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;

/**
 * 3、SQL验证节点
 */
@Slf4j
@Component
public class ValidateSqlNode implements NodeAction {

    @Autowired
    private SqlQueryTools sqlQueryTools;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】SQL验证节点开始执行");

        try {
            // 校验请求参数
            Map<String, Object> nlToSqlResult = state.value(NL_TO_SQL_RESULT, Map.class).orElse(new HashMap<>());
            boolean isValid = (boolean) nlToSqlResult.getOrDefault(SUCCESS, false);
            if (!isValid) {
                return Map.of(
                        ERROR, "【SQL验证节点】自然语言查询转SQL处理失败: " + nlToSqlResult.get(ERROR),
                        WORKFLOW_STATUS, SqlQueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getText());
            }

            String generatedSql = (String) nlToSqlResult.get(GENERATED_SQL);
            if (StrUtil.isBlank(generatedSql) || StrUtil.isBlank(generatedSql.trim())) {
                return Map.of(
                        ERROR, "【SQL验证节点】生成的SQL为空",
                        WORKFLOW_STATUS, SqlQueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getText());
            }

            // 调用QueryTools.validateSql
            Map<String, Object> validateSqlResult = sqlQueryTools.validateSql(generatedSql);
            boolean success = (boolean) validateSqlResult.getOrDefault(SUCCESS, false);
            if (!success) {
                return Map.of(
                        VALIDATE_SQL_RESULT, validateSqlResult,
                        ERROR, "【SQL验证节点】SQL验证失败: " + validateSqlResult.get(ERROR),
                        WORKFLOW_STATUS, SqlQueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getText());
            }

            return Map.of(
                    VALIDATE_SQL_RESULT, validateSqlResult,
                    GENERATED_SQL, generatedSql,
                    WORKFLOW_STATUS, SqlQueryWorkflowStatusEnum.PROCESSING.getId(),
                    CURRENT_NODE, VALIDATE_SQL_NODE.getText(),
                    NEXT_NODE, EXECUTE_SQL_NODE.getText());
        } catch (Exception e) {
            log.error("【数据查询智能体】SQL验证节点执行失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, SqlQueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getText());
        }
    }

}
