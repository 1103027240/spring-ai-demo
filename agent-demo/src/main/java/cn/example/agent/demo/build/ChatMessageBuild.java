package cn.example.agent.demo.build;

import cn.example.agent.demo.entity.ChatMessage;
import cn.example.agent.demo.enums.ChatMessageSyncStatusEnum;
import cn.example.agent.demo.enums.ChatMessageTypeEnum;
import cn.example.agent.demo.mapper.ChatMessageMapper;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ChatMessageBuild {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public ChatMessage buildUserChatMessage(String sessionId, Long userId, String userMessage){
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setUserId(userId);
        userMsg.setMessageType(ChatMessageTypeEnum.USER.getId());
        userMsg.setContent(userMessage);
        userMsg.setIsAiResponse(0);
        userMsg.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getId());
        userMsg.setCreateTime(System.currentTimeMillis());
        return userMsg;
    }

    public ChatMessage buildAiChatMessage(String sessionId, Long userId, String aiResponse) {
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setUserId(userId);
        aiMsg.setMessageType(ChatMessageTypeEnum.AI.getId());
        aiMsg.setContent(aiResponse);
        aiMsg.setIsAiResponse(1);
        aiMsg.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getId());
        aiMsg.setCreateTime(System.currentTimeMillis());
        return aiMsg;
    }

    /**
     * 查询历史对话
     */
    public String buildChatMessageHistory(Long userId, String sessionId, int limit) {
        List<ChatMessage> historyMessages = chatMessageMapper.selectHistoryByUserIdAndSessionId(userId, sessionId, limit);
        if (CollUtil.isEmpty(historyMessages)) {
            return "";
        }

        // 先降序再升序
        CollUtil.reverse(historyMessages);

        // 封装对话历史
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : historyMessages) {
            String role = msg.getIsAiResponse() == 1 ? "AI" : "用户";
            sb.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }

}
