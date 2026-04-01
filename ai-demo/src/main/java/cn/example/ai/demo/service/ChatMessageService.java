package cn.example.ai.demo.service;

import cn.example.ai.demo.param.dto.CustomerServiceStateDto;
import cn.example.ai.demo.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {

    /**
     * 批量保存用户消息和AI消息
     */
    List<ChatMessage> batchSaveMessages(CustomerServiceStateDto state);

    List<ChatMessage> batchSaveMessages(Long userId, String sessionId, String message, String aiResponse);

    void cleanupOldMessageInMysql(Long userId);

    List<ChatMessage> selectUnsyncedMessages(String sessionId);

    List<ChatMessage> selectAllValidMessages(String sessionId);

    void updateMessageSyncStatus(String sessionId);

}

