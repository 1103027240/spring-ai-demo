package cn.example.base.demo.factory;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import java.util.HashMap;
import java.util.Map;

import static cn.example.base.demo.constant.FieldConstant.*;

public class QueryWorkflowFactory {

    public static KeyStrategyFactory queryKeyStrategyFactory() {
        return () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();

            // 输入参数
            strategies.put(QUERY, new ReplaceStrategy());
            strategies.put(NATURAL_LANGUAGE_QUERY, new ReplaceStrategy());

            // 工作流状态
            strategies.put(WORKFLOW_ID, new ReplaceStrategy());
            strategies.put(WORKFLOW_STATUS, new ReplaceStrategy());
            strategies.put(CURRENT_NODE, new ReplaceStrategy());
            strategies.put(NEXT_NODE, new ReplaceStrategy());

            // 智能体处理结果
            strategies.put(QUERY_AGENT_RESULT, new ReplaceStrategy());
            strategies.put(NL_TO_SQL_RESULT, new ReplaceStrategy());
            strategies.put(GENERATED_SQL, new ReplaceStrategy());
            strategies.put(ORIGINAL_QUERY, new ReplaceStrategy());
            strategies.put(QUERY_TYPE, new ReplaceStrategy());
            strategies.put(VALIDATE_SQL_RESULT, new ReplaceStrategy());
            strategies.put(EXECUTE_SQL_RESULT, new ReplaceStrategy());
            strategies.put(ANALYSIS_RESULT, new ReplaceStrategy());
            strategies.put(FINAL_RESULT, new ReplaceStrategy());

            // 最终结果
            strategies.put(SUCCESS, new ReplaceStrategy());
            strategies.put(ERROR, new ReplaceStrategy());

            return strategies;
        };
    }

}
