package cn.example.ai.demo.service;

import cn.example.ai.demo.param.dto.CustomerServiceStateDto;

/**
 * @author 11030
 */
public interface ChatSessionService {

    void createOrUpdateChatSession(CustomerServiceStateDto state);

}
