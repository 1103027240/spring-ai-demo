package cn.example.agent.demo.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模型调用次数和耗时统计
 */
@Slf4j
@HookPositions(value = {HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL})
public class StatisticsModelHook extends ModelHook {

    private static final String MODEL_CALL_COUNT_KEY = "_model_call_count_";

    private static final String MODEL_START_TIME_KEY = "_model_start_time_";

    private static final String MODEL_CALL_TIME_KEY = "_model_call_time_";

    @Override
    public String getName() {
        return "StatisticsModelHook";
    }

    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        log.info("【StatisticsModelHook beforeModel】开始执行");
        Integer modelCallCount = (Integer) config.context().getOrDefault(MODEL_CALL_COUNT_KEY, 0);
        log.info("【StatisticsModelHook】模型调用次数: {}", modelCallCount);

        config.context().put(MODEL_START_TIME_KEY, System.currentTimeMillis());
        return CompletableFuture.completedFuture(Map.of());
    }

    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        log.info("【StatisticsModelHook afterModel】开始执行");
        Integer modelCallCount = (Integer) config.context().getOrDefault(MODEL_CALL_COUNT_KEY, 0);
        config.context().put(MODEL_CALL_COUNT_KEY, modelCallCount + 1);
        
        if(config.context().containsKey(MODEL_START_TIME_KEY)){
            Long startTime = (Long) config.context().get(MODEL_START_TIME_KEY);
            Long durationTime = System.currentTimeMillis() - startTime;

            Long modelCallTime = (Long) config.context().getOrDefault(MODEL_CALL_TIME_KEY, 0L);
            config.context().put(MODEL_CALL_TIME_KEY, modelCallTime + durationTime);
        }
        
        return CompletableFuture.completedFuture(Map.of());
    }

}
