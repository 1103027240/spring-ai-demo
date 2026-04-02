package cn.example.agent.demo.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@HookPositions(value = {HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class LogAgentHook extends AgentHook {

    @Override
    public String getName() {
        return "LogAgentHook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        log.info("【LogAgentHook beforeAgent】开始执行");
        return CompletableFuture.completedFuture(Map.of("startTime", System.currentTimeMillis()));
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        log.info("【LogAgentHook afterAgent】开始执行");
        state.value("startTime").ifPresent(startTime -> {
            Long durationTime = System.currentTimeMillis() - (Long) startTime;
            log.info("【LogAgentHook】模型调用耗时: {}ms", durationTime);
        });
        return CompletableFuture.completedFuture(Map.of());
    }

}
