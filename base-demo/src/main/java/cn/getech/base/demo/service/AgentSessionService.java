package cn.getech.base.demo.service;

public interface AgentSessionService {

    String doChatJsonSession(String message, String sessionId);

    String doChatMysqlSession(String message, String sessionId);

    String doChatSessionManager(String message, String sessionId);

}
