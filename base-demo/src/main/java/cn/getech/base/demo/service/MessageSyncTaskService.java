package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import java.util.List;

public interface MessageSyncTaskService {

    void createSyncTask(CustomerServiceStateDto state, List<ChatMessage> messages);

    void processSyncTask(String sessionId);

}
