package cn.example.base.demo.service;

import cn.example.base.demo.param.dto.CustomerServiceStateDto;

/**
 * @author 11030
 */
public interface ChatSessionService {

    void createOrUpdateChatSession(CustomerServiceStateDto state);

}
