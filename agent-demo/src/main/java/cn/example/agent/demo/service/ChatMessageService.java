package cn.example.agent.demo.service;

import cn.example.agent.demo.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {

    List<ChatMessage> batchSaveMessages(Long userId, String sessionId, String message, String aiResponse);

}

