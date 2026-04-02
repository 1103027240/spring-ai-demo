package cn.example.agent.demo.loader;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.agent.Agent;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public abstract class AbstractAgentLoader implements AgentLoader {

    protected final Map<String, Agent> agents = new ConcurrentHashMap<>();

    @Override
    public @NonNull List<String> listAgents() {
        return agents.keySet().stream().toList();
    }

    @Override
    public Agent loadAgent(String name) {
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("Agent name cannot be null");
        }

        Agent agent = agents.get(name);
        if (agent == null) {
            throw new NoSuchElementException("Agent not found: " + name);
        }
        return agent;
    }

}
