package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.memory.MonitorHook;
import cn.example.ai.demo.service.AgentMcpService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentMcpServiceImpl implements AgentMcpService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Autowired
    private MonitorHook monitorHook;

    @Override
    public String doChat(String message) {
        // 创建MCP客户端
        McpClientWrapper wrapper = McpClientBuilder.create("baidu-mcp-client")
                .stdioTransport("cmd",
                        List.of("/c", "npx", "-y", "@baidumap/mcp-server-baidu-map"),
                        Map.of("BAIDU_MAP_API_KEY", System.getenv("baidu-map-api")))
                .buildAsync()
                .block();

        // 注册MCP
        Toolkit toolkit = new Toolkit();
        toolkit.registerMcpClient(wrapper).block();  //一定要阻塞，让MCP注册工具

        ReActAgent agent = ReActAgent.builder()
                .name("McpAgent")
                .model(qwenAgentChatModel)
                .toolkit(toolkit)
                .hook(monitorHook)
                .build();

        Mono<Msg> responseMono = agent.call(
                Msg.builder()
                        .textContent(message)
                        .build());

        return responseMono.block(Duration.ofSeconds(60)).getTextContent();
    }

}
