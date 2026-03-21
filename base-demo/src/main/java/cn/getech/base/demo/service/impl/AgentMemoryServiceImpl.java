package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.AgentBuild;
import cn.getech.base.demo.service.AgentMemoryService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.Session;
import io.agentscope.core.session.SessionManager;
import io.agentscope.core.session.mysql.MysqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import static cn.getech.base.demo.constant.FieldValueConstant.AGENT_MEMORY_NAME;

@Service
public class AgentMemoryServiceImpl implements AgentMemoryService {

    @Autowired
    private AgentBuild agentBuild;

    @Autowired
    private SessionManager sessionManager;

    @Override
    public String doChat(String message, String sessionId) {
        // 1.创建智能体
        ReActAgent agent = agentBuild.getAgent(AGENT_MEMORY_NAME);

        // 加载Session
        MysqlSession mysqlSession = (MysqlSession) sessionManager.getSession();
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
