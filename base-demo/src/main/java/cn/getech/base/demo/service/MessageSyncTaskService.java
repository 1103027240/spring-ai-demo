package cn.getech.base.demo.service;

import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.MessageSyncTask;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface MessageSyncTaskService {

    void createSyncTask(String sessionId, ChatMessage userMessage, ChatMessage aiMessage) throws JsonProcessingException;

    void processSyncTask(String sessionId) throws JsonProcessingException;

}
