package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.service.AiMemoryService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

/**
 * @author 11030
 */
@Service
public class AiMemoryServiceImpl implements AiMemoryService {

    @Resource(name = "memoryQwenChatClient")
    private ChatClient memoryQwenChatClient;

    @Override
    public String doChat(String msg, String conversationId) {
        return memoryQwenChatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

}
