package cn.getech.base.demo.loader;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * Agent静态加载器
 * 实现 AgentLoader 接口，用于 Spring AI Alibaba Studio 的 Agent 管理
 * @author 11030
 */
@Component
public class AgentStaticLoader implements AgentLoader {

    private final Map<String, BaseAgent> agents = new ConcurrentHashMap<>();

    /**
     * 构造函数，自动注入所有已定义的 Agent Bean
     * @param agentList Spring 容器中的所有 Agent 实例
     */
    @Autowired(required = false)
    public AgentStaticLoader(List<BaseAgent> agentList) {
        if (CollUtil.isNotEmpty(agentList)) {
            for (BaseAgent agent : agentList) {
                this.agents.put(agent.name(), agent);
            }
        }
    }

    /**
     * 获取所有已注册的 Agent 名称列表
     * @return Agent 名称列表
     */
    @Override
    @NotNull
    public List<String> listAgents() {
        return this.agents.keySet().stream().toList();
    }

    /**
     * 根据名称加载指定的 Agent
     * @param name Agent 名称
     * @return Agent 实例
     * @throws NoSuchElementException 如果未找到对应的 Agent
     * @throws IllegalArgumentException 如果名称为空
     */
    @Override
    public BaseAgent loadAgent(String name) {
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("Agent name cannot be null or empty");
        }

        BaseAgent agent = this.agents.get(name);
        if (agent == null) {
            throw new NoSuchElementException("Agent not found: " + name);
        }

        return agent;
    }

    /**
     * 注册单个 Agent（可选方法）
     * @param name Agent 名称
     * @param agent Agent 实例
     */
    public void registerAgent(String name, BaseAgent agent) {
        this.agents.put(name, agent);
    }

}

