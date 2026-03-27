package cn.example.base.demo.node.sql;

import cn.example.base.demo.build.WorkflowBuild;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.QUERY_RESULT_GENERATE;

/**
 * 数据分析节点条件
 */
@Slf4j
@Component
public class SqlResultAnalysisNodeCondition implements EdgeAction {

    @Autowired
    private WorkflowBuild workflowBuild;

    @Override
    public String apply(OverAllState state) throws Exception {
        try {
            Object result = state.value(ANALYSIS_RESULT).orElse(null);
            if (result == null) {
                log.warn("【数据分析智能体节点条件】无输出结果");
                return QUERY_RESULT_GENERATE.getId();
            }

            Map<String, Object> agentResult = new HashMap<>();
            if (result instanceof String str) {
                agentResult = workflowBuild.parseJsonResponse(str);
            } else if (result instanceof Map map) {
                agentResult = map;
            }

            boolean success = (boolean) agentResult.getOrDefault(SUCCESS, false);
            if (!success) {
                log.warn("【数据分析智能体节点条件】分析数据处理失败: {}", agentResult.get(ERROR));
                // 仍然继续，因为可能有简单分析
            }

            return QUERY_RESULT_GENERATE.getId();
        } catch (Exception e) {
            log.error("【数据分析智能体节点条件】分析数据处理失败", e);

            // 仍然继续，因为可能有简单分析
            return QUERY_RESULT_GENERATE.getId();
        }
    }

}
