//package cn.getech.base.demo.service.impl;
//
//import cn.getech.base.demo.dto.CustomerServiceStateDto;
//import cn.getech.base.demo.entity.ChatMessage;
//import cn.getech.base.demo.entity.ChatSession;
//import cn.getech.base.demo.entity.MessageSyncTask;
//import cn.getech.base.demo.entity.WorkflowExecution;
//import cn.getech.base.demo.enums.*;
//import cn.getech.base.demo.mapper.ChatMessageMapper;
//import cn.getech.base.demo.mapper.ChatSessionMapper;
//import cn.getech.base.demo.mapper.MessageSyncTaskMapper;
//import cn.getech.base.demo.mapper.WorkflowExecutionMapper;
//import cn.getech.base.demo.service.ChatSessionService;
//import cn.getech.base.demo.service.WorkflowExecutionService;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.util.StrUtil;
//import com.alibaba.cloud.ai.graph.CompiledGraph;
//import com.alibaba.cloud.ai.graph.OverAllState;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jsqlparser.statement.select.KSQLWindow;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.retry.support.RetryTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.TransactionSynchronization;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static cn.getech.base.demo.contant.RedisKeyConstants.*;
//
///**
// * @author 11030
// */
//@Slf4j
//@Service
//public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {
//
//    // 配置参数
//    @Value("${app.sync.mysql.retention-count:20}")
//    private int mysqlRetentionCount;
//
//    @Value("${app.sync.retry.max-attempts:3}")
//    private int maxRetryAttempts;
//
//    @Value("${app.sync.retry.backoff-delay:1000}")
//    private long retryBackoffDelay;
//
//    @Value("${app.redis.expire.workflow-state:3600}")
//    private int workflowStateExpireSeconds;
//
//    @Value("${app.redis.expire.execution-details:7200}")
//    private int executionDetailsExpireSeconds;
//
//    @Value("${app.redis.expire.session-history:1800}")
//    private int sessionHistoryExpireSeconds;
//
//    @Resource(name = "customerServiceGraph")
//    private CompiledGraph customerServiceGraph;
//
//    @Autowired
//    private ChatSessionService chatSessionService;
//
//    @Autowired
//    private WorkflowExecutionMapper workflowExecutionMapper;
//
//    @Autowired
//    private ChatSessionMapper chatSessionMapper;
//
//    @Autowired
//    private ChatMessageMapper chatMessageMapper;
//
//    @Autowired
//    private MessageSyncTaskMapper messageSyncTaskMapper;
//
//    @Resource(name = "customerKnowledgeVectorStore")
//    private VectorStore vectorStore;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Transactional
//    @Override
//    public Map<String, Object> executeWorkflow(String userInput, Long userId, String userName) {
//        String sessionId = UUID.randomUUID().toString().replace("-", "");
//        String executionId = UUID.randomUUID().toString().replace("-", "");
//        long startTime = System.currentTimeMillis();
//
//        try {
//            // 1.执行工作流
//            CustomerServiceStateDto state = executeWorkflow(executionId, sessionId, userInput, userId, userName);
//
//            // 2.保存执行记录
//            saveWorkflowExecution(executionId, sessionId, userId, userInput, state);
//
//            // 3.创建或更新会话
//            createOrUpdateChatSession(sessionId, userId, userName);
//
//            // 4.保存用户消息
//            ChatMessage userMessage = saveUserMessage(sessionId, userInput, state, executionId);
//
//            // 5.保存AI回复消息
//            ChatMessage aiMessage = saveAiMessage(sessionId, state.getAiResponse(), state, executionId);
//
//            // 6.清理Mysql中的会话消息（Mysql保存最近的N条消息，全量消息保存在Milvus）
//            cleanupOldMessagesInMysql(sessionId);
//
//            // 7.创建同步任务
//            createSyncTask(sessionId, userMessage, aiMessage);
//
//            // 8.缓存状态到Redis
//            cacheStateToRedis(state, executionId);
//
//            // 9.同步会话消息到Milvus
//            registerPostCommitHook(sessionId);
//
//            long duration = System.currentTimeMillis() - startTime;
//            log.info("【主流程】同步处理完成，executionId: {}, 耗时: {}ms", executionId, duration);
//
//            return buildSuccessResponse(state, executionId, duration);
//        } catch (Exception e) {
//            log.error("【主流程】工作流执行失败，executionId: {}", executionId, e);
//
//            // 保存失败记录
//            saveFailureRecord(executionId, sessionId, userId, userInput, e);
//
//            return buildErrorResponse(executionId, e);
//        }
//    }
//
//    /**
//     * 1.执行工作流逻辑
//     */
//    private CustomerServiceStateDto executeWorkflow(String executionId, String sessionId, String userInput, Long userId, String userName) {
//        try {
//            CustomerServiceStateDto state = new CustomerServiceStateDto(sessionId, userInput, userId, userName);
//            state.setExecutionId(executionId);
//            state.setStartTime(System.currentTimeMillis());
//
//            // 准备初始状态
//            Map<String, Object> initialState = new HashMap<>();
//            initialState.put("executionId", executionId);
//            initialState.put("sessionId", sessionId);
//            initialState.put("userId", userId);
//            initialState.put("userName", userName);
//            initialState.put("userInput", userInput);
//
//            // 执行编译后的工作流
//            OverAllState overAllState = OverAllState.from(initialState);
//            GraphExecutionResult result = compiledGraph.execute(overAllState);
//
//            // 处理结果
//            Map<String, Object> output = result.getState().toMap();
//            String aiResponse = extractAiResponse(output);
//
//            // 记录工作流执行结果
//            state.setIntent((String) output.get("intent"));
//            state.setSentiment((String) output.get("sentiment"));
//            state.setKnowledgeContext((String) output.get("knowledge_context"));
//
//            if (output.containsKey("order_results")) {
//                List<Map<String, Object>> orderResults = (List<Map<String, Object>>) output.get("order_results");
//                state.recordNodeResult("order_query", Map.of("results", orderResults));
//            }
//
//            if (output.containsKey("after_sales_result")) {
//                Map<String, Object> afterSalesResult = (Map<String, Object>) output.get("after_sales_result");
//                state.recordNodeResult("after_sales", Map.of("result", afterSalesResult));
//            }
//
//            state.complete("SUCCESS", aiResponse);
//
//            log.debug("工作流逻辑执行完成，aiResponse长度: {}",
//                    aiResponse != null ? aiResponse.length() : 0);
//
//            return state;
//
//        } catch (Exception e) {
//            log.error("工作流逻辑执行失败", e);
//            throw new RuntimeException("工作流逻辑执行失败: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 2.保存执行记录
//     */
//    private void saveWorkflowExecution(String executionId, String sessionId, Long userId, String userInput,
//                                       CustomerServiceStateDto state) throws JsonProcessingException {
//        WorkflowExecution execution = new WorkflowExecution();
//        execution.setExecutionId(executionId);
//        execution.setWorkflowName("customer_service");
//        execution.setSessionId(sessionId);
//        execution.setUserId(userId);
//        execution.setStatus(WorkflowExecutionStatusEnum.SUCCESS.getId());
//        execution.setStartTime(LocalDateTime.now());
//        execution.setEndTime(LocalDateTime.now());
//        execution.setDurationMs(state.getDuration());
//
//        // 输入数据
//        Map<String, Object> inputData = new HashMap<>();
//        inputData.put("userInput", userInput);
//        inputData.put("sessionId", sessionId);
//        inputData.put("userId", userId);
//        inputData.put("timestamp", System.currentTimeMillis());
//        execution.setInputData(objectMapper.writeValueAsString(inputData));
//
//        // 输出数据
//        Map<String, Object> outputData = new HashMap<>();
//        outputData.put("aiResponse", state.getAiResponse());
//        outputData.put("intent", state.getIntent());
//        outputData.put("sentiment", state.getSentiment());
//        outputData.put("workflowStatus", state.getStatus());
//        outputData.put("executionPath", state.getExecutionPath());
//        execution.setOutputData(objectMapper.writeValueAsString(outputData));
//
//        workflowExecutionMapper.insert(execution);
//    }
//
//    /**
//     * 3.创建或更新会话
//     */
//    private void createOrUpdateChatSession(String sessionId, Long userId, String userName) throws JsonProcessingException {
//        // 插入或更新会话
//        ChatSession session = chatSessionMapper.selectBySessionId(sessionId);
//        if (session == null) {
//            session = new ChatSession();
//            session.setSessionId(sessionId);
//            session.setUserId(userId);
//            session.setUserName(userName);
//            session.setSessionType(ChatSessionTypeEnum.CONSULTATION.getCode());
//            session.setStatus(ChatSessionStatusEnum.ACTIVE.getCode());
//            session.setStartTime(LocalDateTime.now());
//            session.setMessageCount(0);
//            session.setCreateTime(LocalDateTime.now());
//            chatSessionMapper.insert(session);
//        } else {
//            session.setLastMessageTime(LocalDateTime.now());
//            chatSessionMapper.updateById(session);
//        }
//
//        // 缓存活跃会话
//        cacheActiveSession(sessionId);
//    }
//
//    /**
//     * 4.保存用户消息
//     */
//    private ChatMessage saveUserMessage(String sessionId, String userInput, CustomerServiceStateDto state, String executionId) {
//        ChatMessage message = new ChatMessage();
//        message.setSessionId(sessionId);
//        message.setMessageType(ChatMessageTypeEnum.USER.getCode());
//        message.setContent(userInput);
//        message.setIntent(state.getIntent());
//        message.setSentiment(state.getSentiment());
//        message.setIsAiResponse(0);
//        message.setWorkflowExecutionId(executionId);
//        message.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getCode());
//        message.setCreateTime(LocalDateTime.now());
//        chatMessageMapper.insert(message);
//        return message;
//    }
//
//    /**
//     * 5.保存AI回复消息
//     */
//    private ChatMessage saveAiMessage(String sessionId, String aiResponse, CustomerServiceStateDto state, String executionId) {
//        if (StrUtil.isBlank(aiResponse)) {
//            log.warn("AI回复为空，跳过保存");
//            return null;
//        }
//
//        ChatMessage message = new ChatMessage();
//        message.setSessionId(sessionId);
//        message.setMessageType(ChatMessageTypeEnum.AI.getCode());
//        message.setContent(aiResponse);
//        message.setIsAiResponse(1);
//        message.setWorkflowExecutionId(executionId);
//        message.setSyncStatus(ChatMessageSyncStatusEnum.PENDING.getCode());
//        message.setCreateTime(LocalDateTime.now());
//
//        // 计算响应时间
//        if (state.getStartTime() != null) {
//            long responseTime = System.currentTimeMillis() - state.getStartTime();
//            message.setResponseTime((int) responseTime);
//        }
//
//        chatMessageMapper.insert(message);
//        return message;
//    }
//
//    /**
//     * 6.清理Mysql中的会话消息
//     */
//    private void cleanupOldMessagesInMysql(String sessionId) {
//        int messageCount = chatMessageMapper.countBySessionId(sessionId);
//        if (messageCount > mysqlRetentionCount) {
//            int messagesToDelete = messageCount - mysqlRetentionCount;
//            List<Long> messageIdsToDelete = chatMessageMapper.selectOldMessageIds(sessionId, messagesToDelete);
//
//            if (CollUtil.isNotEmpty(messageIdsToDelete)) {
//                // 清理Mysql中会话消息
//                chatMessageMapper.batchMarkAsDeleted(messageIdsToDelete);
//                log.info("清理Mysql历史消息完成，sessionId: {}, 删除数量: {}, 保留数量: {}", sessionId, messageIdsToDelete.size(), mysqlRetentionCount);
//
//                // 清理缓存中的会话历史
//                clearCachedSessionHistory(sessionId);
//            }
//        }
//    }
//
//    /**
//     * 7.创建同步任务
//     */
//    private void createSyncTask(String sessionId, ChatMessage userMessage, ChatMessage aiMessage) throws JsonProcessingException {
//        MessageSyncTask task = new MessageSyncTask();
//        task.setSessionId(sessionId);
//        task.setSyncType(MessageTaskSyncTypeEnum.INCREMENTAL.getCode());  // 增量同步
//        task.setStatus(MessageTaskStatusEnum.PENDING.getCode());  // 待处理
//        task.setRetryCount(0);
//        task.setCreatedTime(LocalDateTime.now());
//        task.setUpdatedTime(LocalDateTime.now());
//
//        // 记录最后的消息ID
//        Long lastMessageId = aiMessage != null ? aiMessage.getId() : (userMessage != null ? userMessage.getId() : null);
//        task.setLastMessageId(lastMessageId);
//
//        // 创建同步任务
//        messageSyncTaskMapper.insert(task);
//
//        // 缓存同步任务
//        cacheSyncTask(sessionId, task);
//    }
//
//    /**
//     * 8.缓存状态到Redis
//     */
//    private void cacheStateToRedis(CustomerServiceStateDto state, String executionId) throws JsonProcessingException {
//        String stateKey = WORKFLOW_STATE + executionId;
//        String detailsKey = EXECUTION_DETAIL + executionId;
//        String stateJson = objectMapper.writeValueAsString(state.toMap());
//
//        // 缓存完整状态
//        redisTemplate.opsForValue().set(stateKey, stateJson, workflowStateExpireSeconds, TimeUnit.SECONDS);
//
//        // 缓存执行详情
//        redisTemplate.opsForValue().set(detailsKey, stateJson, executionDetailsExpireSeconds, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 9.注册事务后钩子
//     */
//    private void registerPostCommitHook(String sessionId) {
//        TransactionSynchronizationManager.registerSynchronization(
//                new TransactionSynchronization() {
//                    @Override
//                    public void afterCommit() {
//                        log.info("事务提交成功，触发异步同步，sessionId: {}", sessionId);
//                        triggerAsyncSync(sessionId);
//                    }
//                }
//        );
//    }
//
//    /**
//     * 缓存活跃会话
//     */
//    private void cacheActiveSession(String sessionId) throws JsonProcessingException {
//        String sessionKey = SESSION_ACTIVE + sessionId;
//        Map<String, Object> sessionInfo = new HashMap<>();
//        sessionInfo.put("sessionId", sessionId);
//        sessionInfo.put("lastActiveTime", System.currentTimeMillis());
//        sessionInfo.put("cachedTime", LocalDateTime.now().toString());
//
//        String sessionJson = objectMapper.writeValueAsString(sessionInfo);
//        redisTemplate.opsForValue().set(sessionKey, sessionJson, 3600, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 清理缓存中的会话历史
//     */
//    private void clearCachedSessionHistory(String sessionId) {
//        String historyKey = SESSION_HISTORY + sessionId;
//        redisTemplate.delete(historyKey);
//    }
//
//    /**
//     * 缓存同步任务
//     */
//    private void cacheSyncTask(String sessionId, MessageSyncTask task) throws JsonProcessingException {
//        String taskKey = SYNC_TASKS + ":" + sessionId;
//        String taskJson = objectMapper.writeValueAsString(task);
//        redisTemplate.opsForValue().set(taskKey, taskJson, 3600, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 触发异步同步
//     */
//    private void triggerAsyncSync(String sessionId) {
//        // 在实际生产环境中，这里应该将任务提交到消息队列
//        new Thread(() -> {
//            try {
//                Thread.sleep(100);  // 稍微延迟，确保事务完全提交
//                processSyncTask(sessionId);
//            } catch (Exception e) {
//                log.error("异步同步处理失败", e);
//            }
//        }).start();
//    }
//
//    /**
//     * 异步同步处理
//     */
//    public void processSyncTask(String sessionId) {
//        MessageSyncTask task = null;
//        try {
//            // 1.首先从Redis获取缓存的同步任务，如果获取不到，再从数据库获取
//            task = getCachedSyncTask(sessionId);
//            if (task == null) {
//                task = messageSyncTaskMapper.selectLatestPendingTask(sessionId);
//            }
//            if (task == null) {
//                log.info("没有找到待处理的同步任务，sessionId: {}", sessionId);
//                return;
//            }
//
//            // 2.更新任务状态为处理中
//            task.setStatus(1);
//            task.setUpdatedTime(LocalDateTime.now());
//            messageSyncTaskMapper.updateById(task);
//
//            // 3.同步到Milvus
//            RetryTemplate retryTemplate = createRetryTemplate();
//            MessageSyncTask finalTask = task;
//            retryTemplate.execute(context -> {
//                log.info("【异步同步】开始同步消息到Milvus，sessionId: {}, 重试次数: {}", sessionId, context.getRetryCount() + 1);
//
//                boolean syncSuccess = syncMessagesToMilvus(sessionId, finalTask.getSyncType());
//                if (!syncSuccess) {
//                    throw new RuntimeException("会话消息同步到Milvus失败");
//                }
//                return null;
//            });
//
//            // 4.更新任务状态为完成
//            task.setStatus(2);
//            task.setUpdatedTime(LocalDateTime.now());
//            messageSyncTaskMapper.updateById(task);
//
//            // 更新消息同步状态
//            updateMessageSyncStatus(sessionId);
//
//            // 清理缓存的同步任务
//            clearCachedSyncTask(sessionId);
//
//            log.info("【异步同步】同步任务处理成功，sessionId: {}", sessionId);
//        } catch (Exception e) {
//            log.error("【异步同步】处理同步任务失败，sessionId: {}", sessionId, e);
//            if (task != null) {
//                // 更新任务状态为失败
//                task.setStatus(3);
//                task.setRetryCount(task.getRetryCount() + 1);
//                task.setErrorMessage(e.getMessage());
//                task.setUpdatedTime(LocalDateTime.now());
//                messageSyncTaskMapper.updateById(task);
//
//                // 更新缓存的同步任务
//                cacheSyncTask(sessionId, task);
//            }
//            throw e;
//        }
//    }
//
//    /**
//     * 获取缓存的同步任务
//     */
//    private MessageSyncTask getCachedSyncTask(String sessionId) {
//        try {
//            String taskKey = SYNC_TASKS + ":" + sessionId;
//            String taskJson = (String) redisTemplate.opsForValue().get(taskKey);
//            if (StrUtil.isNotBlank(taskJson)) {
//                return objectMapper.readValue(taskJson, MessageSyncTask.class);
//            }
//        } catch (Exception e) {
//            log.error("获取缓存的同步任务失败", e);
//        }
//        return null;
//    }
//
//}
