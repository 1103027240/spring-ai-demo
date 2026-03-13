package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.enums.ChatMessageSyncStatusEnum;
import cn.getech.base.demo.enums.ChatMessageTypeEnum;
import cn.getech.base.demo.mapper.ChatMessageMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Value("${sync.mysql.retention.count:20}")
    private String mysqlRetentionCount;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Transactional
    @Override
    public ChatMessage saveUserMessage(CustomerServiceStateDto state) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(state.getSessionId());
        message.setUserId(state.getUserId());
        message.setMessageType(ChatMessageTypeEnum.USER.getCode());
        message.setContent(state.getUserInput());
        message.setIntent(state.getIntent());
        message.setSentiment(state.getSentiment());
        message.setIsAiResponse(0);
        message.setWorkflowExecutionId(state.getExecutionId());
        message.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getCode());
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    @Transactional
    @Override
    public ChatMessage saveAiMessage(CustomerServiceStateDto state) {
        if (StrUtil.isBlank(state.getAiResponse())) {
            log.warn("【售后客服工作流】AI回复为空，跳过保存");
            return null;
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(state.getSessionId());
        message.setUserId(state.getUserId());
        message.setMessageType(ChatMessageTypeEnum.AI.getCode());
        message.setContent(state.getAiResponse());
        message.setIsAiResponse(1);
        message.setWorkflowExecutionId(state.getExecutionId());
        message.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getCode());
        message.setCreateTime(LocalDateTime.now());

        // 计算响应时间
        if (state.getStartTime() != null) {
            long responseTime = System.currentTimeMillis() - state.getStartTime();
            message.setResponseTime((int) responseTime);
        }

        chatMessageMapper.insert(message);
        return message;
    }

    /**
     * 清理Mysql中的会话消息
     */
    @Transactional
    @Override
    public void cleanupOldMessageInMysql(Long userId) {
        int mysqlRetentionMaxCount = Integer.parseInt(mysqlRetentionCount);
        int messageCount = chatMessageMapper.countByUserId(userId);

        if (messageCount > mysqlRetentionMaxCount) {
            int messagesDelete = messageCount - mysqlRetentionMaxCount;
            List<Long> messageIdsDelete = chatMessageMapper.selectOldMessageIds(userId, messagesDelete);
            if (CollUtil.isNotEmpty(messageIdsDelete)) {
                chatMessageMapper.batchMarkAsDeleted(messageIdsDelete);
            }
        }
    }

    @Transactional
    @Override
    public void updateMessageSyncStatus(String sessionId) {
        try {
            int updatedCount = chatMessageMapper.updateSyncStatusToSynced(sessionId);
            log.info("【异步同步】更新消息同步状态为已完成，sessionId: {}, 更新数量: {}", sessionId, updatedCount);
        } catch (Exception e) {
            log.error("【异步同步】更新消息同步状态失败", e);
        }
    }

    @Override
    public List<ChatMessage> selectUnsyncedMessages(String sessionId) {
        return chatMessageMapper.selectUnsyncedMessages(sessionId);
    }

    @Override
    public List<ChatMessage> selectAllValidMessages(String sessionId) {
        return chatMessageMapper.selectAllValidMessages(sessionId);
    }

}
