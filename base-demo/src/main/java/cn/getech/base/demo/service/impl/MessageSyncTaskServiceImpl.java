package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.MessageSyncTask;
import cn.getech.base.demo.enums.ChatMessageTypeEnum;
import cn.getech.base.demo.enums.MessageTaskStatusEnum;
import cn.getech.base.demo.mapper.MessageSyncTaskMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.getech.base.demo.service.MessageSyncTaskService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static cn.getech.base.demo.constant.RedisKeyConstant.SYNC_TASKS;
import static cn.getech.base.demo.enums.MessageTaskSyncTypeEnum.INCREMENTAL;

@Slf4j
@Service
public class MessageSyncTaskServiceImpl implements MessageSyncTaskService {

    @Autowired
    private MessageSyncTaskMapper messageSyncTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Resource(name = "customerKnowledgeVectorStore")
    private VectorStore vectorStore;

    @Override
    public void createSyncTask(String sessionId, ChatMessage userMessage, ChatMessage aiMessage) throws JsonProcessingException {
        MessageSyncTask task = new MessageSyncTask();
        task.setSessionId(sessionId);
        task.setSyncType(INCREMENTAL.getCode());  // 增量同步
        task.setStatus(MessageTaskStatusEnum.PENDING.getCode());  // 待处理
        task.setRetryCount(0);
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());

        // 记录最后的消息ID
        Long lastMessageId = aiMessage != null ? aiMessage.getId() : (userMessage != null ? userMessage.getId() : null);
        task.setLastMessageId(lastMessageId);

        // 创建同步任务
        messageSyncTaskMapper.insert(task);

        // 缓存同步任务
        cacheSyncTask(sessionId, task);
    }

    /**
     * 异步同步处理（全量消息保存在Milvus）
     */
    @Override
    public void processSyncTask(String sessionId) throws JsonProcessingException {
        MessageSyncTask task = null;
        try {
            // 1.首先从Redis获取缓存的同步任务，如果获取不到，再从数据库获取
            task = getCachedSyncTask(sessionId);
            if (task == null) {
                task = messageSyncTaskMapper.selectLatestPendingTask(sessionId);
            }
            if (task == null) {
                log.info("没有找到待处理的同步任务，sessionId: {}", sessionId);
                return;
            }

            // 2.更新任务状态为处理中
            task.setStatus(MessageTaskStatusEnum.PROCESSING.getCode());
            task.setUpdatedTime(LocalDateTime.now());
            messageSyncTaskMapper.updateById(task);

            // 3.同步到Milvus
            boolean syncSuccess = syncMessagesToMilvus(sessionId, task.getSyncType());
            if (!syncSuccess) {
                throw new RuntimeException("会话消息同步到Milvus失败");
            }

            // 4.更新任务状态为已完成
            task.setStatus(MessageTaskStatusEnum.COMPLETED.getCode());
            task.setUpdatedTime(LocalDateTime.now());
            messageSyncTaskMapper.updateById(task);

            // 5.更新消息同步状态同步状态为已同步
            chatMessageService.updateMessageSyncStatus(sessionId);

            // 6.清理缓存的同步任务
            clearCachedSyncTask(sessionId);

            log.info("【异步同步】同步任务处理成功，sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("【异步同步】处理同步任务失败，sessionId: {}", sessionId, e);
            if (task != null) {
                // 更新任务状态为失败
                task.setStatus(MessageTaskStatusEnum.FAILED.getCode());
                task.setRetryCount(task.getRetryCount() + 1);
                task.setErrorMessage(e.getMessage());
                task.setUpdatedTime(LocalDateTime.now());
                messageSyncTaskMapper.updateById(task);

                // 更新缓存的同步任务
                cacheSyncTask(sessionId, task);
            }
            throw e;
        }
    }

    /**
     * 同步消息到Milvus
     */
    private boolean syncMessagesToMilvus(String sessionId, Integer syncType) {
        try {
            List<ChatMessage> messagesToSync;

            if (syncType == INCREMENTAL.getCode()) {
                // 增量同步：同步未同步的消息
                messagesToSync = chatMessageService.selectUnsyncedMessages(sessionId);
                log.info("【Milvus同步】增量同步，sessionId: {}, 消息数量: {}", sessionId, messagesToSync.size());
            } else {
                // 全量同步：同步所有有效消息
                messagesToSync = chatMessageService.selectAllValidMessages(sessionId);
                log.info("【Milvus同步】全量同步，sessionId: {}, 消息数量: {}", sessionId, messagesToSync.size());
            }

            if (CollUtil.isEmpty(messagesToSync)) {
                log.info("没有需要同步的消息，sessionId: {}", sessionId);
                return true;
            }

            // 构建文档列表
            List<Document> documents = new ArrayList<>();
            for (ChatMessage message : messagesToSync) {
                try {
                    documents.add(buildMilvusDocument(message));
                } catch (Exception e) {
                    log.error("构建Milvus文档失败，消息ID: {}", message.getId(), e);
                }
            }

            if (CollUtil.isEmpty(documents)) {
                log.warn("没有有效的文档需要同步，sessionId: {}", sessionId);
                return true;
            }

            // 批量保存到Milvus
            vectorStore.add(documents);

            return true;
        } catch (Exception e) {
            log.error("同步消息到Milvus失败，sessionId: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 获取缓存的同步任务
     */
    private MessageSyncTask getCachedSyncTask(String sessionId) {
        try {
            String taskKey = SYNC_TASKS + ":" + sessionId;
            String taskJson = (String) redisTemplate.opsForValue().get(taskKey);
            if (StrUtil.isNotBlank(taskJson)) {
                return objectMapper.readValue(taskJson, MessageSyncTask.class);
            }
        } catch (Exception e) {
            log.error("获取缓存的同步任务失败", e);
        }
        return null;
    }

    /**
     * 清理缓存的同步任务
     */
    private void clearCachedSyncTask(String sessionId) {
        try {
            String taskKey = SYNC_TASKS + ":" + sessionId;
            redisTemplate.delete(taskKey);
        } catch (Exception e) {
            log.error("清理缓存的同步任务失败", e);
        }
    }

    /**
     * 缓存同步任务
     */
    public void cacheSyncTask(String sessionId, MessageSyncTask task) throws JsonProcessingException {
        String taskKey = SYNC_TASKS + ":" + sessionId;
        String taskJson = objectMapper.writeValueAsString(task);
        redisTemplate.opsForValue().set(taskKey, taskJson, 3600, TimeUnit.SECONDS);
    }

    /**
     * 构建Milvus文档
     */
    private Document buildMilvusDocument(ChatMessage message) {
        StringBuilder content = new StringBuilder();
        content.append("消息类型: ").append(ChatMessageTypeEnum.getDescription(message.getMessageType()));
        content.append("\n内容: ").append(message.getContent());

        if (StrUtil.isNotBlank(message.getIntent())) {
            content.append("\n意图: ").append(message.getIntent());
        }

        if (StrUtil.isNotBlank(message.getSentiment())) {
            content.append("\n情感: ").append(message.getSentiment());
        }

        Document document = new Document(content.toString());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("messageId", message.getId());
        metadata.put("sessionId", message.getSessionId());
        metadata.put("messageType", message.getMessageType());
        metadata.put("messageTypeText", ChatMessageTypeEnum.getDescription(message.getMessageType()));
        metadata.put("isAi", message.getIsAiResponse());
        metadata.put("workflowExecutionId", message.getWorkflowExecutionId());
        metadata.put("createTime", message.getCreateTime().toString());
        metadata.put("syncTime", LocalDateTime.now().toString());
        metadata.put("source", "mysql_sync");

        if (message.getResponseTime() != null) {
            metadata.put("responseTimeMs", message.getResponseTime());
        }

        if (StrUtil.isNotBlank(message.getIntent())) {
            metadata.put("intent", message.getIntent());
        }

        if (StrUtil.isNotBlank(message.getSentiment())) {
            metadata.put("sentiment", message.getSentiment());
        }

        document.getMetadata().putAll(metadata);
        return document;
    }

}
