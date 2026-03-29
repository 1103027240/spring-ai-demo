package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.StudioBuild;
import cn.example.base.demo.service.AgentStudioService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.Model;
import io.agentscope.core.studio.StudioManager;
import io.agentscope.core.studio.StudioMessageHook;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentStudioServiceImpl implements AgentStudioService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Autowired
    private StudioBuild studioBuild;

    @Override
    public String doChat(String message) {
        try {
            studioBuild.initStudio("可视化智能体");

            ReActAgent agent = ReActAgent.builder()
                    .name("可视化智能体")
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
            log.error("【可视化智能体】执行失败", e);
            return e.getMessage();
        } finally {
            StudioManager.shutdown();
        }
    }

}
