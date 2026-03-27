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
 * 2、自然语言查询转SQL节点
 */
@Slf4j
@Component
public class NlToSqlNode implements NodeAction {

    @Autowired
    private QueryTools queryTools;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】自然语言查询转SQL节点开始执行");

        try {
            // 校验请求参数
            String naturalLanguageQuery = state.value(NATURAL_LANGUAGE_QUERY, String.class).orElse("");
            if (StrUtil.isBlank(naturalLanguageQuery)) {
                return Map.of(
                        ERROR, "【转SQL节点】自然语言查询为空",
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getText());
            }

            // 调用QueryTools.nlToSql
            Map<String, Object> nlToSqlResult = queryTools.nlToSql(naturalLanguageQuery);
            boolean success = (boolean) nlToSqlResult.getOrDefault(SUCCESS, false);
            if (!success) {
                return Map.of(
                        NL_TO_SQL_RESULT, nlToSqlResult,
                        ERROR, "【转SQL节点】自然语言查询转SQL处理失败: " + nlToSqlResult.get(ERROR),
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getText());
            }

            String generatedSql = (String) nlToSqlResult.getOrDefault(GENERATED_SQL, "");
            if (StrUtil.isBlank(generatedSql) || StrUtil.isBlank(generatedSql.trim())) {
                return Map.of(
                        NL_TO_SQL_RESULT, nlToSqlResult,
                        ERROR, "【转SQL节点】生成的SQL为空",
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getText());
            }

            return Map.of(
                    NL_TO_SQL_RESULT, nlToSqlResult,
                    GENERATED_SQL, generatedSql,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.PROCESSING.getId(),
                    CURRENT_NODE, NL_TO_SQL_NODE.getText(),
                    NEXT_NODE, VALIDATE_SQL_NODE.getText());
        } catch (Exception e) {
            log.error("【数据查询智能体】自然语言查询转SQL节点执行失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getText());
        }
    }

}
