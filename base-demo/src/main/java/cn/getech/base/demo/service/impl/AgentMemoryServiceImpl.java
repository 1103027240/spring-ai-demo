package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.AgentBuild;
import cn.getech.base.demo.service.AgentMemoryService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
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
import static cn.getech.base.demo.constant.FieldValueConstant.AGENT_MEMORY_NAME;

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
                .name(AGENT_MEMORY_NAME)
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
