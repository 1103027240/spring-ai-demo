package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.AgentDemoService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AgentDemoServiceImpl implements AgentDemoService {

//    @Resource(name = "qwenAgentChatModel")
//    private Model qwenAgentChatModel;

    @Override
    public String doChatModelQwen(String msg) {
//        ReActAgent agent = ReActAgent.builder()
//                .name("agentDemo")
//                .sysPrompt("你是一个AI助手")
//                .model(qwenAgentChatModel)
//                .build();
//
//        Msg agentMsg = Msg.builder()
//                .textContent("你好")
//                .build();
//
//        return agent.call(agentMsg).block().getTextContent();

        return null;
    }

}
