package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.build.AgentBuild;
import cn.example.ai.demo.service.AgentMemoryService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.LongTermMemoryMode;
import io.agentscope.core.memory.reme.ReMeLongTermMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.Model;
import io.agentscope.core.session.Session;
import io.agentscope.core.session.mysql.MysqlSession;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

@Service
public class AgentMemoryServiceImpl implements AgentMemoryService {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AgentBuild agentBuild;

    @Override
    public String doChat(String message, String userId, String sessionId) {
        ReMeLongTermMemory longTermMemory = ReMeLongTermMemory.builder()
                .userId(userId)
                .apiBaseUrl("http://localhost:8002")  //需要搭建reme服务，reme服务端口
                .build();

        // 1.创建智能体
        ReActAgent agent = ReActAgent.builder()
                .name("MemoryAgent")
                .model(qwenAgentChatModel)
                .longTermMemory(longTermMemory)
                .longTermMemoryMode(LongTermMemoryMode.STATIC_CONTROL)
                .build();

        // 加载Session
        Session mysqlSession = new MysqlSession(dataSource, true);
        agent.loadIfExists(mysqlSession, sessionId);

        // 2.创建消息
        Msg msg = agentBuild.getMsg(message);

        // 3.调用大模型
        String response = agent.call(msg).block().getTextContent();

        // 4.保存会话
        agent.saveTo(mysqlSession, sessionId);
        return response;
    }

}
