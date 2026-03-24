package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.AgentStudioService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
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
                .project("AgentStudio")
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();

        ReActAgent agent = ReActAgent.builder()
                .name("agentStudio")
                .model(qwenAgentChatModel)
                .hook(new StudioMessageHook(StudioManager.getClient()))
                .build();

        Msg msg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("从以下位置提取用户请求: " + message)
                        .build())
                .build();

        String response = agent.call(msg).block().getTextContent();

        // 清理资源
        StudioManager.shutdown();
        return response;
    }

}
