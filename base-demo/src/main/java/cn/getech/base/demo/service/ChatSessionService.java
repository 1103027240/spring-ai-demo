package cn.getech.base.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author 11030
 */
public interface ChatSessionService {

    void createOrUpdateChatSession(String sessionId, Long userId, String userName) throws JsonProcessingException;

}
