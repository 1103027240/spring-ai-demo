package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.ContactInfoVO;
import cn.getech.base.demo.service.AgentStructuredService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AgentStructuredServiceImpl implements AgentStructuredService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    /**
     * 结构化输出
     * @param message 请求参数有ContactInfoVO数据
     * @return 结构化输出结果
     */
    @Override
    public ContactInfoVO doChat(String message) {
        ReActAgent agent = ReActAgent.builder()
                .name("agentStructured")
                .model(qwenAgentChatModel)
                .sysPrompt("你是一个智能分析助手。分析用户请求，并提供结构化回复")
                .build();

        Msg msg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("从以下位置提取用户请求: " + message)
                        .build())
                .build();

        Msg response = agent.call(msg, ContactInfoVO.class).block();
        return response.getStructuredData(ContactInfoVO.class);
    }

}
