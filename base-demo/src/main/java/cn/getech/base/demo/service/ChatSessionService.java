package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author 11030
 */
public interface ChatSessionService {

    void createOrUpdateChatSession(CustomerServiceStateDto state) throws JsonProcessingException;

}
