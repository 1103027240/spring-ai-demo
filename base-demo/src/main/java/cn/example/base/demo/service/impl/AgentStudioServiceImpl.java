package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.AgentStudioService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.Model;
import io.agentscope.core.studio.StudioManager;
import io.agentscope.core.studio.StudioMessageHook;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AgentStudioServiceImpl implements AgentStudioService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Override
    public String doChat(String message) {
        // 初始化Studio连接
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("StudioAgent")
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();

        try {
            ReActAgent agent = ReActAgent.builder()
                    .name("StudioAgent")
                    .model(qwenAgentChatModel)
                    .hook(new StudioMessageHook(StudioManager.getClient()))
                    .build();

            Msg msg = Msg.builder()
                    .textContent(message)
                    .build();

            return agent.call(msg)
                    .block()
                    .getTextContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 清理资源
            StudioManager.shutdown();
        }
    }

}
