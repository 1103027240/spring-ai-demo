package cn.example.ai.demo.config.routing;

import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingGraphBuildingStrategy;
import com.alibaba.cloud.ai.graph.agent.flow.strategy.FlowGraphBuildingStrategyRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 手动注册AGENT_SCOPE_ROUTING策略，保存到FlowGraphBuildingStrategyRegistry（strategyFactories）中
 * 因为FlowGraphBuildingStrategyRegistry.registerDefaultStrategies方法没有注册AGENT_SCOPE_ROUTING策略
 */
@Slf4j
@Configuration
public class AgentScopeRoutingStrategyConfig {

    @PostConstruct
    public void registerAgentScopeRoutingStrategy() {
        log.info("【AgentScopeRoutingStrategyConfig】开始手动注册AGENT_SCOPE_ROUTING策略...");

        try {
            FlowGraphBuildingStrategyRegistry registry = FlowGraphBuildingStrategyRegistry.getInstance();

            // 手动注册AGENT_SCOPE_ROUTING策略，value为Supplier<AgentScopeRoutingGraphBuildingStrategy>
            registry.registerStrategy(AgentScopeRoutingGraphBuildingStrategy.AGENT_SCOPE_ROUTING_TYPE, AgentScopeRoutingGraphBuildingStrategy::new);

            log.info("【AgentScopeRoutingStrategyConfig】手动注册AGENT_SCOPE_ROUTING策略成功...");
        } catch (Exception e) {
            log.error("【AgentScopeRoutingStrategyConfig】手动注册AGENT_SCOPE_ROUTING策略失败...", e);
        }
    }

}
