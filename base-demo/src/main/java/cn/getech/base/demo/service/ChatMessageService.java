package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {

    ChatMessage saveUserMessage(String sessionId, String userInput, CustomerServiceStateDto state, String executionId);

    ChatMessage saveAiMessage(String sessionId, String aiResponse, CustomerServiceStateDto state, String executionId);

    void cleanupOldMessageInMysql(String sessionId);

    List<ChatMessage> selectUnsyncedMessages(String sessionId);

    List<ChatMessage> selectAllValidMessages(String sessionId);

    void updateMessageSyncStatus(String sessionId);

}
