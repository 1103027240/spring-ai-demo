package cn.example.base.demo.factory;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;

public class CodeSearchFactory {

    public static KeyStrategyFactory codeSearchKeyStrategyFactory() {
        return () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put(QUERY, new ReplaceStrategy());
            strategies.put(USER_INPUT, new ReplaceStrategy());
            strategies.put(MERGED_RESULT, new ReplaceStrategy());
            strategies.put(FINAL_RESULT, new ReplaceStrategy());
            return strategies;
        };
    }

}
