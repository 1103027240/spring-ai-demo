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
    public ChatMessage saveUserMessage(String sessionId, String userInput, CustomerServiceStateDto state, String executionId) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setMessageType(ChatMessageTypeEnum.USER.getCode());
        message.setContent(userInput);
        message.setIntent(state.getIntent());
        message.setSentiment(state.getSentiment());
        message.setIsAiResponse(0);
        message.setWorkflowExecutionId(executionId);
        message.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getCode());
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public ChatMessage saveAiMessage(String sessionId, String aiResponse, CustomerServiceStateDto state, String executionId) {
        if (StrUtil.isBlank(aiResponse)) {
            log.warn("AI回复为空，跳过保存");
            return null;
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setMessageType(ChatMessageTypeEnum.AI.getCode());
        message.setContent(aiResponse);
        message.setIsAiResponse(1);
        message.setWorkflowExecutionId(executionId);
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
    public void cleanupOldMessageInMysql(String sessionId) {
        int messageCount = chatMessageMapper.countBySessionId(sessionId);
        if (messageCount > mysqlRetentionCount) {
            int messagesDelete = messageCount - mysqlRetentionCount;
            List<Long> messageIdsDelete = chatMessageMapper.selectOldMessageIds(sessionId, messagesDelete);

            if (CollUtil.isNotEmpty(messageIdsDelete)) {
                // 清理Mysql中会话消息
                chatMessageMapper.batchMarkAsDeleted(messageIdsDelete);

                // 重新加载会话消息
                reloadCacheChatMessage(sessionId);
            }
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

    @Override
    public void updateMessageSyncStatus(String sessionId) {
        try {
            int updatedCount = chatMessageMapper.updateSyncStatusToSynced(sessionId);
            log.info("更新消息同步状态完成，sessionId: {}, 更新数量: {}", sessionId, updatedCount);
        } catch (Exception e) {
            log.error("更新消息同步状态失败", e);
        }
    }

    /**
     * 重新加载会话消息（Mysql保存最近的N条消息）
     */
    private void reloadCacheChatMessage(String sessionId) {
        try {
            // 1. 清理现有缓存
            clearCacheChatMessage(sessionId);

            // 2. 重新加载缓存
            List<Map<String, Object>> chatMessageFromDatabases = getChatMessageFromDatabase(sessionId, mysqlRetentionCount);
            if (CollUtil.isNotEmpty(chatMessageFromDatabases)) {
                cacheChatMessage(sessionId, chatMessageFromDatabases);
                log.debug("会话消息缓存重新加载完成，sessionId: {}, 消息数量: {}", sessionId, chatMessageFromDatabases.size());
            } else {
                log.debug("没有找到会话消息，sessionId: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("重新加载会话消息缓存失败，sessionId: {}", sessionId, e);
        }
    }

    /**
     * 缓存消息到Redis
     */
    private void cacheChatMessage(String sessionId, List<Map<String, Object>> historyList) {
        try {
            String historyJson = objectMapper.writeValueAsString(historyList);
            String historyKey = SESSION_HISTORY + sessionId;
            redisTemplate.opsForValue().set(historyKey, historyJson, sessionHistoryExpireSeconds, TimeUnit.SECONDS);
            log.debug("会话历史已缓存到Redis，sessionId: {}, 消息数量: {}", sessionId, historyList.size());
        } catch (Exception e) {
            log.error("缓存会话历史到Redis失败", e);
        }
    }

    /**
     * 清理缓存中的会话消息
     */
    private void clearCacheChatMessage(String sessionId) {
        String chatMessageKey = SESSION_HISTORY + sessionId;
        redisTemplate.delete(chatMessageKey);
    }

    /**
     * 从数据库获取会话消息（不经过Redis缓存）
     */
    private List<Map<String, Object>> getChatMessageFromDatabase(String sessionId, int limit) {
        // Redis中没有，从数据库查询
        List<ChatMessage> messages = chatMessageMapper.selectBySessionId(sessionId, limit);
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
