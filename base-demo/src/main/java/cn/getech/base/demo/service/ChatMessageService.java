package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {

    ChatMessage saveUserMessage(CustomerServiceStateDto state);

    ChatMessage saveAiMessage(CustomerServiceStateDto state);

    void cleanupOldMessageInMysql(Long userId);

    List<ChatMessage> selectUnsyncedMessages(String sessionId);

    List<ChatMessage> selectAllValidMessages(String sessionId);

    void updateMessageSyncStatus(String sessionId);

}
