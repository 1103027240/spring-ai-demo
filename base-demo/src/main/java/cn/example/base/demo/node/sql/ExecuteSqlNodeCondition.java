package cn.example.base.demo.node.sql;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.ANALYSIS_NODE;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.ERROR_HANDLE_NODE;

/**
 * SQL执行节点条件
 */
@Slf4j
@Component
public class ExecuteSqlNodeCondition implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        String error = state.value(ERROR, String.class).orElse("");
        Map<String, Object> executeSqlResult = state.value(EXECUTE_SQL_RESULT, Map.class).orElse(new HashMap<>());
        boolean success = (boolean) executeSqlResult.getOrDefault(SUCCESS, false);

        if (StrUtil.isNotBlank(error)) {
            return ERROR_HANDLE_NODE.getId();
        }

        if (!success) {
            return ERROR_HANDLE_NODE.getId();
        }

        return ANALYSIS_NODE.getId();
    }

}
