package cn.example.base.demo.service;

import cn.example.base.demo.param.dto.CustomerServiceStateDto;
import cn.example.base.demo.entity.ChatMessage;
import java.util.List;

public interface MessageSyncTaskService {

    void createSyncTask(CustomerServiceStateDto state, List<ChatMessage> messages);

    void processSyncTask(String sessionId);

}
