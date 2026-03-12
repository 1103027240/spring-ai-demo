package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.ChatSession;
import cn.getech.base.demo.entity.MessageSyncTask;
import cn.getech.base.demo.entity.WorkflowExecution;
import cn.getech.base.demo.enums.*;
import cn.getech.base.demo.mapper.ChatMessageMapper;
import cn.getech.base.demo.mapper.ChatSessionMapper;
import cn.getech.base.demo.mapper.MessageSyncTaskMapper;
import cn.getech.base.demo.mapper.WorkflowExecutionMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.getech.base.demo.service.ChatSessionService;
import cn.getech.base.demo.service.MessageSyncTaskService;
import cn.getech.base.demo.service.WorkflowExecutionService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static cn.getech.base.demo.constant.RedisKeyConstant.*;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.AFTER_SALES;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.ORDER_QUERY;
import static cn.getech.base.demo.enums.WorkflowExecutionStatusEnum.SUCCESS;

/**
 * @author 11030
 */
@Slf4j
@Service
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    @Value("${app.redis.expire.workflow-state:3600}")
    private int workflowStateExpireSeconds;

    @Value("${app.redis.expire.execution-details:7200}")
    private int executionDetailsExpireSeconds;

    @Resource(name = "customerServiceGraph")
    private CompiledGraph compiledGraph;

    @Autowired
    private WorkflowExecutionMapper workflowExecutionMapper;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageSyncTaskService messageSyncTaskService;

    @Resource(name = "customerKnowledgeVectorStore")
    private VectorStore vectorStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生产环境加分布式锁处理
     */
    @Transactional
    @Override
    public Map<String, Object> executeWorkflow(String userInput, Long userId, String userName) {
        String executionId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();

        try {
            // 1.执行工作流
            CustomerServiceStateDto state = executeWorkflow(executionId, sessionId, userInput, userId, userName);

            // 2.保存执行记录
            saveWorkflowExecution(executionId, sessionId, userId, userInput, state);

            // 3.创建或更新会话
            chatSessionService.createOrUpdateChatSession(sessionId, userId, userName);

            // 4.保存用户消息
            ChatMessage userMessage = chatMessageService.saveUserMessage(sessionId, userInput, state, executionId);

            // 5.保存AI回复消息
            ChatMessage aiMessage = chatMessageService.saveAiMessage(sessionId, state.getAiResponse(), state, executionId);

            // 6.清理Mysql中的会话消息（Mysql保存最近的N条消息）
            chatMessageService.cleanupOldMessageInMysql(sessionId);

            // 7.创建同步任务
            messageSyncTaskService.createSyncTask(sessionId, userMessage, aiMessage);

            // 8.缓存工作流状态到Redis
            cacheCustomerServiceState(state, executionId);

            // 9.事务提交之后，异步同步会话消息到Milvus
            registerPostCommitHook(sessionId);

            long duration = System.currentTimeMillis() - startTime;
            log.info("【主流程】同步处理完成，executionId: {}, 耗时: {}ms", executionId, duration);

            return buildSuccessResponse(state, executionId, duration);
        } catch (Exception e) {
            log.error("【主流程】工作流执行失败，executionId: {}", executionId, e);

            saveFailureRecord(executionId, sessionId, userId, userInput, e);

            return buildErrorResponse(executionId, e);
        }
    }

    /**
     * 执行工作流逻辑
     */
    private CustomerServiceStateDto executeWorkflow(String executionId, String sessionId, String userInput, Long userId, String userName) {
        try {
            Map<String, Object> initialState = new HashMap<>();
            initialState.put("executionId", executionId);
            initialState.put("sessionId", sessionId);
            initialState.put("userId", userId);
            initialState.put("userName", userName);
            initialState.put("userInput", userInput);

            // 1.执行工作流
            OverAllState overAllState = new OverAllState(initialState);
            RunnableConfig config = RunnableConfig.builder().build();
            Map<String, Object> output = compiledGraph.invoke(overAllState, config)
                    .map(OverAllState::data)
                    .orElseThrow(() -> new RuntimeException("【售后客服工作流】执行未返回结果"));

            // 2.封装工作流状态
            CustomerServiceStateDto state = new CustomerServiceStateDto(executionId, sessionId, userInput, userId, userName, System.currentTimeMillis());
            state.setIntent((String) output.get("intent"));
            state.setSentiment((String) output.get("sentiment"));
            state.setKnowledgeContext((String) output.get("knowledgeContext"));

            if (output.containsKey("orderResults")) {
                List<Map<String, Object>> orderResults = (List<Map<String, Object>>) output.get("orderResults");
                state.recordNodeResult(ORDER_QUERY.getId(), Map.of("results", orderResults));
            }

            if (output.containsKey("afterSalesResult")) {
                Map<String, Object> afterSalesResult = (Map<String, Object>) output.get("afterSalesResult");
                state.recordNodeResult(AFTER_SALES.getId(), Map.of("result", afterSalesResult));
            }

            String aiResponse = extractAiResponse(output);
            state.complete(SUCCESS.getId(), aiResponse);

            return state;
        } catch (Exception e) {
            log.error("【售后客服工作流】执行失败", e);
            throw new RuntimeException("【售后客服工作流】执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存成功执行记录
     */
    private void saveWorkflowExecution(String executionId, String sessionId, Long userId, String userInput,
                                       CustomerServiceStateDto state) throws JsonProcessingException {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setExecutionId(executionId);
        execution.setWorkflowName("customer_service");
        execution.setSessionId(sessionId);
        execution.setUserId(userId);
        execution.setStatus(SUCCESS.getId());
        execution.setStartTime(LocalDateTime.now());
        execution.setEndTime(LocalDateTime.now());
        execution.setDurationMs(state.getDuration());

        // 输入数据
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userInput", userInput);
        inputData.put("sessionId", sessionId);
        inputData.put("userId", userId);
        inputData.put("timestamp", System.currentTimeMillis());
        execution.setInputData(objectMapper.writeValueAsString(inputData));

        // 输出数据
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("aiResponse", state.getAiResponse());
        outputData.put("intent", state.getIntent());
        outputData.put("sentiment", state.getSentiment());
        outputData.put("workflowStatus", state.getStatus());
        outputData.put("executionPath", state.getExecutionPath());
        execution.setOutputData(objectMapper.writeValueAsString(outputData));
        workflowExecutionMapper.insert(execution);
    }

    /**
     * 保存失败执行记录
     */
    private void saveFailureRecord(String executionId, String sessionId, Long userId, String userInput, Exception e) {
        try {
            WorkflowExecution execution = new WorkflowExecution();
            execution.setExecutionId(executionId);
            execution.setWorkflowName("customer_service");
            execution.setSessionId(sessionId);
            execution.setUserId(userId);
            execution.setStatus("failed");
            execution.setStartTime(LocalDateTime.now());
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());

            Map<String, Object> inputData = new HashMap<>();
            inputData.put("userInput", userInput);
            inputData.put("sessionId", sessionId);
            inputData.put("userId", userId);
            execution.setInputData(objectMapper.writeValueAsString(inputData));

            workflowExecutionMapper.insert(execution);
        } catch (Exception ex) {
            log.error("保存失败记录失败", ex);
        }
    }

    /**
     * 缓存状态到Redis
     */
    private void cacheCustomerServiceState(CustomerServiceStateDto state, String executionId) throws JsonProcessingException {
        String stateKey = WORKFLOW_STATE + executionId;
        String detailsKey = EXECUTION_DETAIL + executionId;
        String stateJson = objectMapper.writeValueAsString(state.toMap());

        // 缓存完整状态
        redisTemplate.opsForValue().set(stateKey, stateJson, workflowStateExpireSeconds, TimeUnit.SECONDS);

        // 缓存执行详情
        redisTemplate.opsForValue().set(detailsKey, stateJson, executionDetailsExpireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 注册事务后钩子
     */
    private void registerPostCommitHook(String sessionId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("【售后客服工作流】事务提交成功，触发异步同步，sessionId: {}", sessionId);

                        // 生产环境，推送到MQ消息队列
                        new Thread(() -> {
                            try {
                                Thread.sleep(100);  // 稍微延迟，确保事务完全提交
                                messageSyncTaskService.processSyncTask(sessionId);
                            } catch (Exception e) {
                                log.error("【售后客服工作流】异步同步处理失败", e);
                            }
                        }).start();
                    }
                }
        );
    }

    /**
     * 从输出中提取AI回复
     */
    private String extractAiResponse(Map<String, Object> output) {
        if (output.containsKey("aiResponse")) {
            Object aiResponseObj = output.get("aiResponse");
            if (aiResponseObj instanceof String) {
                return (String) aiResponseObj;
            } else if (aiResponseObj != null) {
                return aiResponseObj.toString();
            }
        }

        // 如果AI回复不存在，尝试从其他可能的键中获取
        for (String key : Arrays.asList("response", "answer", "content", "message")) {
            if (output.containsKey(key)) {
                Object responseObj = output.get(key);
                if (responseObj instanceof String) {
                    return (String) responseObj;
                } else if (responseObj != null) {
                    return responseObj.toString();
                }
            }
        }

        return "您好，请问有什么可以帮助您的？";
    }

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccessResponse(CustomerServiceStateDto state, String executionId, long duration) {
        Map<String, Object> response = new HashMap<>();
        response.put("executionId", executionId);
        response.put("status", "success");
        response.put("durationMs", duration);
        response.put("aiResponse", state.getAiResponse());
        response.put("intent", state.getIntent());
        response.put("sentiment", state.getSentiment());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 构建失败响应
     */
    private Map<String, Object> buildErrorResponse(String executionId, Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("executionId", executionId);
        response.put("status", "error");
        response.put("error", e.getMessage());
        response.put("aiResponse", "抱歉，系统处理您的请求时出现了问题，请稍后重试。");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

}
