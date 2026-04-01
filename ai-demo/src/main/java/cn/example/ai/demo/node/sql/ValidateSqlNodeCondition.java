package cn.example.ai.demo.node.sql;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.ai.demo.constant.FieldConstant.*;
import static cn.example.ai.demo.enums.SqlQueryNodeEnum.ERROR_HANDLE_NODE;
import static cn.example.ai.demo.enums.SqlQueryNodeEnum.EXECUTE_SQL_NODE;

/**
 * SQL验证节点条件
 */
@Slf4j
@Component
public class ValidateSqlNodeCondition implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        String error = state.value(ERROR, String.class).orElse("");
        Map<String, Object> validateSqlResult = state.value(VALIDATE_SQL_RESULT, Map.class).orElse(new HashMap<>());
        boolean success = (boolean) validateSqlResult.getOrDefault(SUCCESS, false);

        if (StrUtil.isNotBlank(error)) {
            return ERROR_HANDLE_NODE.getText();
        }

        if (!success) {
            return ERROR_HANDLE_NODE.getText();
        }

        return EXECUTE_SQL_NODE.getText();
    }

}
