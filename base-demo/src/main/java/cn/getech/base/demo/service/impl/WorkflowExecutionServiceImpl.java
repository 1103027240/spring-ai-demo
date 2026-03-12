package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.constant.RedisKeyConstant;
import cn.getech.base.demo.constant.WorkflowConstant;
import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.dto.WorkflowRequestDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.WorkflowExecution;
import cn.getech.base.demo.enums.CustomerServiceNodeEnum;
import cn.getech.base.demo.enums.WorkflowExecutionStatusEnum;
import cn.getech.base.demo.mapper.WorkflowExecutionMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.getech.base.demo.service.ChatSessionService;
import cn.getech.base.demo.service.MessageSyncTaskService;
import cn.getech.base.demo.service.WorkflowExecutionService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.getech.base.demo.constant.RedisKeyConstant.*;
import static cn.getech.base.demo.constant.WorkflowConstant.*;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.AFTER_SALES;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.ORDER_QUERY;
import static cn.getech.base.demo.enums.WorkflowExecutionStatusEnum.SUCCESS;

/**
 * 工作流执行服务实现
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
    private CompiledGraph customerServiceGraph;

    @Autowired
    private WorkflowExecutionMapper workflowExecutionMapper;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageSyncTaskService messageSyncTaskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "workflowExecutor")
    private Executor workflowExecutor;

    /**
     * 生产环境加分布式锁处理
     */
    @Transactional
    @Override
    public Map<String, Object> executeWorkflow(String userInput, Long userId, String userName) {
        WorkflowRequestDto dto = new WorkflowRequestDto(userInput, userId, userName);
        return executeWorkflow(dto);
    }

    /**
     * 执行工作流（DTO版本）
     */
    @Transactional
    public Map<String, Object> executeWorkflow(WorkflowRequestDto dto) {
        String executionId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();
        log.info("【主流程】开始执行工作流，executionId: {}, sessionId: {}, userId: {}", executionId, sessionId, dto.getUserId());

        try {
            // 1.执行工作流
            CustomerServiceStateDto state = executeWorkflowInternal(executionId, sessionId, dto);

            // 2.保存执行记录
            saveWorkflowExecution(executionId, sessionId, dto, state);

            // 3.创建或更新会话
            chatSessionService.createOrUpdateChatSession(sessionId, dto.getUserId(), dto.getUserName());

            // 4.保存用户消息
            ChatMessage userMessage = chatMessageService.saveUserMessage(sessionId, dto.getUserInput(), state, executionId);

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
            saveFailureRecord(executionId, sessionId, dto, e);
            return buildErrorResponse(executionId, e);
        }
    }

    /**
     * 执行工作流逻辑
     */
    private CustomerServiceStateDto executeWorkflowInternal(String executionId, String sessionId, WorkflowRequestDto dto) {
        try {
            Map<String, Object> initialState = new HashMap<>();
            initialState.put("executionId", executionId);
            initialState.put("sessionId", sessionId);
            initialState.put("userId", dto.getUserId());
            initialState.put("userName", dto.getUserName());
            initialState.put("userInput", dto.getUserInput());

            OverAllState overAllState = new OverAllState(initialState);
            RunnableConfig config = RunnableConfig.builder().build();

            Map<String, Object> output = customerServiceGraph.invoke(overAllState, config)
                    .map(OverAllState::data)
                    .orElseThrow(() -> new RuntimeException("【售后客服工作流】执行未返回结果"));

            return buildCustomerServiceState(executionId, sessionId, dto, output);
        } catch (Exception e) {
            log.error("【售后客服工作流】执行失败", e);
            throw new RuntimeException("【售后客服工作流】执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建客服状态DTO
     */
    private CustomerServiceStateDto buildCustomerServiceState(String executionId, String sessionId, WorkflowRequestDto dto,
                                                              Map<String, Object> output) {
        CustomerServiceStateDto state = new CustomerServiceStateDto(executionId, sessionId, dto.getUserInput(), dto.getUserId(), dto.getUserName(), System.currentTimeMillis());

        state.setIntent((String) output.get(INTENT));
        state.setSentiment((String) output.get(SENTIMENT));
        state.setKnowledgeContext((String) output.get(KNOWLEDGE_CONTEXT));

        if (output.containsKey(ORDER_RESULTS)) {
            List<Map<String, Object>> orderResults = (List<Map<String, Object>>) output.get(ORDER_RESULTS);
            state.recordNodeResult(ORDER_QUERY.getId(), Map.of("results", orderResults));
        }

        if (output.containsKey(AFTER_SALES_RESULT)) {
            Map<String, Object> afterSalesResult = (Map<String, Object>) output.get(AFTER_SALES_RESULT);
            state.recordNodeResult(AFTER_SALES.getId(), Map.of("result", afterSalesResult));
        }

        String aiResponse = extractAiResponse(output);
        state.complete(SUCCESS.getId(), aiResponse);

        return state;
    }

    /**
     * 保存成功执行记录
     */
    private void saveWorkflowExecution(String executionId, String sessionId, WorkflowRequestDto request,
                                       CustomerServiceStateDto state) throws JsonProcessingException {
        WorkflowExecution execution = createBaseWorkflowExecution(executionId, sessionId, request, SUCCESS.getId());
        execution.setDurationMs(state.getDuration());
        execution.setInputData(buildInputDataJson(request, sessionId));
        execution.setOutputData(buildOutputDataJson(state));
        workflowExecutionMapper.insert(execution);
    }

    /**
     * 保存失败执行记录
     */
    private void saveFailureRecord(String executionId, String sessionId, WorkflowRequestDto request, Exception e) {
        try {
            WorkflowExecution execution = createBaseWorkflowExecution(executionId, sessionId, request, WorkflowExecutionStatusEnum.FAILED.getId());
            execution.setErrorMessage(e.getMessage());
            execution.setInputData(buildInputDataJson(request, sessionId));
            workflowExecutionMapper.insert(execution);
        } catch (Exception ex) {
            log.error("保存失败记录失败", ex);
        }
    }

    /**
     * 创建基础工作流执行记录
     */
    private WorkflowExecution createBaseWorkflowExecution(String executionId, String sessionId,
                                                          WorkflowRequestDto request, String status) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setExecutionId(executionId);
        execution.setWorkflowName(WORKFLOW_CUSTOMER_SERVICE);
        execution.setSessionId(sessionId);
        execution.setUserId(request.getUserId());
        execution.setStatus(status);
        execution.setStartTime(LocalDateTime.now());
        execution.setEndTime(LocalDateTime.now());
        return execution;
    }

    /**
     * 构建输入数据JSON
     */
    private String buildInputDataJson(WorkflowRequestDto request, String sessionId) throws JsonProcessingException {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userInput", request.getUserInput());
        inputData.put("sessionId", sessionId);
        inputData.put("userId", request.getUserId());
        inputData.put("userName", request.getUserName());
        inputData.put("timestamp", System.currentTimeMillis());
        return objectMapper.writeValueAsString(inputData);
    }

    /**
     * 构建输出数据JSON
     */
    private String buildOutputDataJson(CustomerServiceStateDto state) throws JsonProcessingException {
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("aiResponse", state.getAiResponse());
        outputData.put("intent", state.getIntent());
        outputData.put("sentiment", state.getSentiment());
        outputData.put("workflowStatus", state.getStatus());
        outputData.put("executionPath", state.getExecutionPath());
        return objectMapper.writeValueAsString(outputData);
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
                        asyncProcessSyncTask(sessionId);
                    }
                }
        );
    }

    /**
     * 异步处理同步任务
     */
    @Async("workflowExecutor")
    private void asyncProcessSyncTask(String sessionId) {
        try {
            Thread.sleep(TRANSACTION_COMMIT_DELAY_MS);
            messageSyncTaskService.processSyncTask(sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【售后客服工作流】异步同步处理被中断，sessionId: {}", sessionId, e);
        } catch (Exception e) {
            log.error("【售后客服工作流】异步同步处理失败，sessionId: {}", sessionId, e);
        }
    }

    /**
     * 从输出中提取AI回复
     */
    private String extractAiResponse(Map<String, Object> output) {
        for (String key : AI_RESPONSE_KEYS) {
            if (output.containsKey(key)) {
                Object aiResponseObj = output.get(key);
                if (aiResponseObj != null) {
                    return aiResponseObj.toString();
                }
            }
        }
        return DEFAULT_AI_RESPONSE;
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
        response.put("aiResponse", ERROR_AI_RESPONSE);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

}
