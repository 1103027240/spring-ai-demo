package cn.example.ai.demo.build;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentBuild {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    public ReActAgent getAgent(InMemoryMemory memory, String agentName) {
        return ReActAgent.builder()
                .name(agentName)
                .model(qwenAgentChatModel)
                .memory(memory)
                .build();
    }

    public Msg getMsg(String message) {
        return Msg.builder()
                .content(TextBlock.builder().text(message).build())
                .build();
    }

}
