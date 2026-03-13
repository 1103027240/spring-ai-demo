package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.CustomerKnowledgeBuild;
import cn.getech.base.demo.build.MessageSyncTaskBuild;
import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.MessageSyncTask;
import cn.getech.base.demo.enums.MessageTaskStatusEnum;
import cn.getech.base.demo.mapper.MessageSyncTaskMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.getech.base.demo.service.MessageSyncTaskService;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static cn.getech.base.demo.constant.FieldValueConstant.SYNC_TASK_CACHE_EXPIRE_SECONDS;
import static cn.getech.base.demo.constant.RedisKeyConstant.SYNC_TASKS;

/**
 * 消息同步任务服务实现
 * @author 11030
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSyncTaskServiceImpl implements MessageSyncTaskService {

    @Autowired
    private MessageSyncTaskMapper messageSyncTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageSyncTaskBuild messageSyncTaskBuild;

    @Autowired
    private CustomerKnowledgeBuild customerKnowledgeBuild;

    @Override
    public void createSyncTask(CustomerServiceStateDto state, List<ChatMessage> messages) {
        ChatMessage userMessage = messages.get(0);
        ChatMessage aiMessage = messages.size() > 1 ? messages.get(1) : null;
        MessageSyncTask task = messageSyncTaskBuild.buildSyncTask(state, userMessage, aiMessage);

        // 创建同步任务
        messageSyncTaskMapper.insert(task);

        // 缓存同步任务
        cacheSyncTask(state.getSessionId(), task);
    }

    /**
     * 异步同步处理（增加消息保存在mysql，全量消息保存在Milvus）
     */
    @Override
    public void processSyncTask(String sessionId) {
        // 1.首先从Redis获取缓存同步任务，如果获取不到，再从数据库获取
        MessageSyncTask task = loadSyncTask(sessionId);
        if (task == null) {
            log.info("【异步同步】没有找到待处理的同步任务，sessionId: {}", sessionId);
            return;
        }

        try {
            // 2.更新任务状态为处理中
            updateTaskStatus(task, MessageTaskStatusEnum.PROCESSING.getCode());

            // 3.会话消息同步到Milvus
            if (!customerKnowledgeBuild.syncMessagesToMilvus(sessionId, task.getSyncType())) {
                throw new RuntimeException("会话消息同步到Milvus失败");
            }

            // 4.更新任务状态为已完成
            updateTaskStatus(task, MessageTaskStatusEnum.COMPLETED.getCode());

            // 5.更新会话消息同步状态为已同步
            chatMessageService.updateMessageSyncStatus(sessionId);

            // 6.清理缓存的同步任务
            clearCachedSyncTask(sessionId);

            log.info("【异步同步】同步任务处理成功，sessionId: {}", sessionId);
        } catch (Exception e) {
            handleSyncFailure(task, sessionId, e);
            throw e;
        }
    }

    /**
     * 加载同步任务
     */
    public MessageSyncTask loadSyncTask(String sessionId) {
        MessageSyncTask task = getCachedSyncTask(sessionId);
        if (task == null) {
            task = messageSyncTaskMapper.selectLatestPendingTask(sessionId);
        }
        return task;
    }

    /**
     * 更新任务状态
     */
    public void updateTaskStatus(MessageSyncTask task, Integer status) {
        task.setStatus(status);
        task.setUpdatedTime(LocalDateTime.now());
        messageSyncTaskMapper.updateById(task);
    }

    /**
     * 处理同步失败
     */
    public void handleSyncFailure(MessageSyncTask task, String sessionId, Exception e) {
        log.error("【异步同步】处理同步任务失败，sessionId: {}", sessionId, e);
        if (task != null) {
            task.setStatus(MessageTaskStatusEnum.FAILED.getCode());
            task.setRetryCount(task.getRetryCount() + 1);
            task.setErrorMessage(e.getMessage());
            task.setUpdatedTime(LocalDateTime.now());
            messageSyncTaskMapper.updateById(task);
            cacheSyncTask(sessionId, task);
        }
    }

    /**
     * 获取缓存同步任务
     */
    public MessageSyncTask getCachedSyncTask(String sessionId) {
        String taskKey = SYNC_TASKS + ":" + sessionId;
        try {
            String taskJson = (String) redisTemplate.opsForValue().get(taskKey);
            if (StrUtil.isNotBlank(taskJson)) {
                return objectMapper.readValue(taskJson, MessageSyncTask.class);
            }
        } catch (Exception e) {
            log.error("【异步同步】获取缓存同步任务失败", e);
        }
        return null;
    }

    /**
     * 清理缓存同步任务
     */
    public void clearCachedSyncTask(String sessionId) {
        String taskKey = SYNC_TASKS + ":" + sessionId;
        try {
            redisTemplate.delete(taskKey);
        } catch (Exception e) {
            log.error("【异步同步】清理缓存同步任务失败", e);
        }
    }

    /**
     * 缓存同步任务
     */
    public void cacheSyncTask(String sessionId, MessageSyncTask task) {
        String taskKey = SYNC_TASKS + ":" + sessionId;
        try {
            String taskJson = objectMapper.writeValueAsString(task);
            redisTemplate.opsForValue().set(taskKey, taskJson, SYNC_TASK_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
