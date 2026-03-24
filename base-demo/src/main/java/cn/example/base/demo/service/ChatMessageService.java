package cn.example.base.demo.service;

import cn.example.base.demo.dto.CustomerServiceStateDto;
import cn.example.base.demo.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {

    /**
     * 批量保存用户消息和AI消息
     */
    List<ChatMessage> batchSaveMessages(CustomerServiceStateDto state);

    void cleanupOldMessageInMysql(Long userId);

    List<ChatMessage> selectUnsyncedMessages(String sessionId);

    List<ChatMessage> selectAllValidMessages(String sessionId);

    void updateMessageSyncStatus(String sessionId);

}

