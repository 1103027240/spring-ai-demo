package cn.getech.base.demo.build;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.enums.ChatMessageSyncStatusEnum;
import cn.getech.base.demo.enums.ChatMessageTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageBuild {

    public ChatMessage buildUserChatMessage(CustomerServiceStateDto state){
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(state.getSessionId());
        userMessage.setUserId(state.getUserId());
        userMessage.setMessageType(ChatMessageTypeEnum.USER.getId());
        userMessage.setContent(state.getUserInput());
        userMessage.setIntent(state.getIntent());
        userMessage.setSentiment(state.getSentiment());
        userMessage.setIsAiResponse(0);
        userMessage.setWorkflowExecutionId(state.getExecutionId());
        userMessage.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getId());
        userMessage.setCreateTime(System.currentTimeMillis());
        return userMessage;
    }

    public ChatMessage buildAiChatMessage(CustomerServiceStateDto state){
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(state.getSessionId());
        aiMessage.setUserId(state.getUserId());
        aiMessage.setMessageType(ChatMessageTypeEnum.AI.getId());
        aiMessage.setContent(state.getAiResponse());
        aiMessage.setIsAiResponse(1);
        aiMessage.setWorkflowExecutionId(state.getExecutionId());
        aiMessage.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getId());
        aiMessage.setCreateTime(System.currentTimeMillis());

        // 计算响应时间
        if (state.getStartTime() != null) {
            long responseTime = System.currentTimeMillis() - state.getStartTime();
            aiMessage.setResponseTime((int) responseTime);
        }

        return aiMessage;
    }

}
