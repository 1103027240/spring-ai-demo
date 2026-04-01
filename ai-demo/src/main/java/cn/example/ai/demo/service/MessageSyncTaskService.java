package cn.example.ai.demo.service;

import cn.example.ai.demo.param.dto.CustomerServiceStateDto;
import cn.example.ai.demo.entity.ChatMessage;
import java.util.List;

public interface MessageSyncTaskService {

    void createSyncTask(CustomerServiceStateDto state, List<ChatMessage> messages);

    void processSyncTask(String sessionId);

}
