package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;

/**
 * @author 11030
 */
public interface ChatSessionService {

    void createOrUpdateChatSession(CustomerServiceStateDto state);

}
