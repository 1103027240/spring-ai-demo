package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.enums.ChatMessageSyncStatusEnum;
import cn.getech.base.demo.enums.ChatMessageTypeEnum;
import cn.getech.base.demo.mapper.ChatMessageMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static cn.getech.base.demo.constant.RedisKeyConstant.SESSION_HISTORY;

@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Value("${app.redis.expire.session-history:1800}")
    private int sessionHistoryExpireSeconds;

    @Value("${app.sync.mysql.retention-count:20}")
    private int mysqlRetentionCount;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    @Override
    public ChatMessage saveAiMessage(CustomerServiceStateDto state) {
        if (StrUtil.isBlank(state.getAiResponse())) {
            log.warn("【售后客服工作流】AiResponse()AI回复为空，跳过保存");
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
    @Override
    public void cleanupOldMessageInMysql(Long userId) {
        int messageCount = chatMessageMapper.countByUserId(userId);
        if (messageCount > mysqlRetentionCount) {
            int messagesDelete = messageCount - mysqlRetentionCount;
            List<Long> messageIdsDelete = chatMessageMapper.selectOldMessageIds(userId, messagesDelete);

            if (CollUtil.isNotEmpty(messageIdsDelete)) {
                // 清理Mysql中会话消息
                chatMessageMapper.batchMarkAsDeleted(messageIdsDelete);

                // 重新加载会话消息
                reloadCacheChatMessage(userId);
            }
        }
    }

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

    /**
     * 重新加载会话消息（Mysql保存最近的N条消息）
     */
    private void reloadCacheChatMessage(Long userId) {
        try {
            // 1. 清理现有缓存
            clearCacheChatMessage(userId);

            // 2. 重新加载缓存
            List<Map<String, Object>> chatMessages = getChatMessageFromDatabase(userId, mysqlRetentionCount);
            if (CollUtil.isNotEmpty(chatMessages)) {
                cacheChatMessage(userId, chatMessages);
                log.debug("会话消息缓存重新加载完成，userId: {}, 消息数量: {}", userId, chatMessages.size());
            } else {
                log.debug("从数据库中没有找到会话消息，userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("重新加载会话消息缓存失败，userId: {}", userId, e);
        }
    }

    /**
     * 缓存会话消息到Redis
     */
    private void cacheChatMessage(Long userId, List<Map<String, Object>> chatMessages) {
        try {
            String chatMessageKey = SESSION_HISTORY + userId;
            String chatMessageJson = objectMapper.writeValueAsString(chatMessages);
            redisTemplate.opsForValue().set(chatMessageKey, chatMessageJson, sessionHistoryExpireSeconds, TimeUnit.SECONDS);
            log.debug("会话消息已缓存到Redis，userId: {}, 消息数量: {}", userId, chatMessages.size());
        } catch (Exception e) {
            log.error("缓存会话消息到Redis失败", e);
        }
    }

    /**
     * 清理缓存中的会话消息
     */
    private void clearCacheChatMessage(Long userId) {
        String chatMessageKey = SESSION_HISTORY + userId;
        redisTemplate.delete(chatMessageKey);
    }

    /**
     * 从数据库获取会话消息（不经过Redis缓存）
     */
    private List<Map<String, Object>> getChatMessageFromDatabase(Long userId, int limit) {
        List<ChatMessage> messages = chatMessageMapper.selectByUserId(userId, limit);
        return messages.stream()
                .map(msg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", msg.getId());
                    map.put("sessionId", msg.getSessionId());
                    map.put("messageType", ChatMessageTypeEnum.getDescription(msg.getMessageType()));
                    map.put("content", msg.getContent());
                    map.put("intent", msg.getIntent());
                    map.put("sentiment", msg.getSentiment());
                    map.put("isAi", msg.getIsAiResponse());
                    map.put("workflowExecutionId", msg.getWorkflowExecutionId());
                    map.put("createTime", msg.getCreateTime());
                    map.put("syncStatus", msg.getSyncStatus());
                    return map;
                }).collect(java.util.stream.Collectors.toList());
    }

}
