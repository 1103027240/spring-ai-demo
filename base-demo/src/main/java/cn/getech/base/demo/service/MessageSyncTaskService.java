package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.MessageSyncTask;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface MessageSyncTaskService {

    void createSyncTask(CustomerServiceStateDto state, ChatMessage userMessage, ChatMessage aiMessage);

    void processSyncTask(String sessionId);

}
