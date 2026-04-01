package cn.example.ai.demo.factory;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.agent.flow.node.RoutingMergeNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import java.util.HashMap;
import java.util.Map;
import static cn.example.ai.demo.constant.FieldConstant.*;

public class CodeSearchFactory {

    public static KeyStrategyFactory codeSearchKeyStrategyFactory() {
        return () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put(QUERY, new ReplaceStrategy());
            strategies.put(USER_INPUT, new ReplaceStrategy());
            strategies.put(FINAL_RESULT, new ReplaceStrategy());
            strategies.put(RoutingMergeNode.DEFAULT_MERGED_OUTPUT_KEY, new ReplaceStrategy());
            strategies.put(GITHUB_RESULT, new ReplaceStrategy());
            strategies.put(GITEE_RESULT, new ReplaceStrategy());
            strategies.put(CSDN_RESULT, new ReplaceStrategy());
            strategies.put(GITHUB_INPUT, new ReplaceStrategy());
            strategies.put(GITEE_INPUT, new ReplaceStrategy());
            strategies.put(CSDN_INPUT, new ReplaceStrategy());
            return strategies;
        };
    }

}
