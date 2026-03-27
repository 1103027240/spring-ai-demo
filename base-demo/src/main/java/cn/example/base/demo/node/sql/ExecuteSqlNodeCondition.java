package cn.example.base.demo.node.sql;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static cn.example.base.demo.constant.FieldConstant.ERROR;
import static cn.example.base.demo.constant.FieldConstant.SUCCESS;
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
        boolean success = state.value(SUCCESS, Boolean.class).orElse(false);
        String error = state.value(ERROR, String.class).orElse("");

        if (StrUtil.isBlank(error)) {
            return ERROR_HANDLE_NODE.getId();
        }

        if (!success) {
            return ERROR_HANDLE_NODE.getId();
        }

        return ANALYSIS_NODE.getId();
    }

}
