package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.AgentBuild;
import cn.example.base.demo.service.AgentSessionService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import io.agentscope.core.session.SessionManager;
import io.agentscope.core.session.mysql.MysqlSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.nio.file.Path;

@Slf4j
@Service
public class AgentSessionServiceImpl implements AgentSessionService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AgentBuild agentBuild;

    @Override
    public String doChatJsonSession(String message, String sessionId) {
        // 1.创建智能体
        InMemoryMemory memory = new InMemoryMemory();
        ReActAgent agent = agentBuild.getAgent(memory, "SessionAgent");

        // 创建JsonSession并加载
        Session jsonSession = new JsonSession(Path.of("E:\\sessions"));
        loadIfExists(agent, jsonSession, sessionId);

        // 2.创建消息
        Msg msg = agentBuild.getMsg(message);

        // 3.调用大模型
        String response = agent.call(msg).block().getTextContent();

        // 4.保存会话
        agent.saveTo(jsonSession, sessionId);
        return response;
    }

    @Override
    public String doChatMysqlSession(String message, String sessionId) {
        // 1.创建智能体
        InMemoryMemory memory = new InMemoryMemory();
        ReActAgent agent = agentBuild.getAgent(memory, "SessionAgent");

        // 创建MysqlSession并加载
        Session mysqlSession = new MysqlSession(dataSource, true);
        loadIfExists(agent, mysqlSession, sessionId);

        // 2.创建消息
        Msg msg = agentBuild.getMsg(message);

        // 3.调用大模型
        String response = agent.call(msg).block().getTextContent();

        // 4.保存会话
        agent.saveTo(mysqlSession, sessionId);
        return response;
    }

    @Override
    public String doChatSessionManager(String message, String sessionId) {
        // 1.创建智能体
        InMemoryMemory memory = new InMemoryMemory();
        ReActAgent agent = agentBuild.getAgent(memory, "SessionAgent");

        // 创建SessionManager并加载
        SessionManager sessionManager = SessionManager.forSessionId(sessionId)
                .withSession(new MysqlSession(dataSource, true))
                .addComponent(agent)
                .addComponent(memory);
        sessionManager.loadIfExists();

        // 2.创建消息
        Msg msg = agentBuild.getMsg(message);

        // 3.调用大模型
        String response = agent.call(msg).block().getTextContent();

        // 4.保存会话
        sessionManager.saveSession();
        return response;
    }

    public void loadIfExists(ReActAgent agent, Session session, String sessionId) {
        if (agent.loadIfExists(session, sessionId)) {
            log.info("会话已加载: {}", sessionId);
        } else {
            log.info("新会话已创建: {}", sessionId);
        }
    }

}
