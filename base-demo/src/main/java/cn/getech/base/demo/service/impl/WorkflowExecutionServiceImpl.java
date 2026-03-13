package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.CustomerServiceStateBuild;
import cn.getech.base.demo.build.WorkflowExecutionBuild;
import cn.getech.base.demo.constant.RedisKeyConstant;
import cn.getech.base.demo.constant.WorkflowConstant;
import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.dto.MessageDocumentVO;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
import java.util.stream.Collectors;
import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.getech.base.demo.constant.FieldValueConstant.CREATE_TIME;
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

    @Autowired
    private CustomerServiceStateBuild customerServiceStateBuild;

    @Autowired
    private WorkflowExecutionBuild workflowExecutionBuild;

    @Resource(name = "customerKnowledgeVectorStore")
    private VectorStore customerKnowledgeVectorStore;

    /**
     * 生产环境加分布式锁处理
     */
    @Transactional
    @Override
    public Map<String, Object> executeWorkflow(String userInput, Long userId, String userName) {
        WorkflowRequestDto dto = new WorkflowRequestDto(userInput, userId, userName);
        return executeWorkflow(dto);
    }

    @Override
    public Page<MessageDocumentVO> pageChatHistory(Long userId, String currentPage, String pageSize) {
        int page = Integer.parseInt(currentPage);
        int size = Integer.parseInt(pageSize);

        List<MessageDocumentVO> messages = queryMilvusByUserId(userId, page, size);
        Page<MessageDocumentVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(messages);
        resultPage.setTotal(messages.size());
        return resultPage;
    }

    /**
     * 执行工作流
     */
    @Transactional
    public Map<String, Object> executeWorkflow(WorkflowRequestDto dto) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String executionId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();

        // 1.执行工作流
        CustomerServiceStateDto state = executeWorkflowInternal(executionId, sessionId, dto);

        // 2.保存执行记录
        saveWorkflowExecution(state);

        // 3.创建或更新会话
        chatSessionService.createOrUpdateChatSession(state);

        // 4.保存用户消息
        ChatMessage userMessage = chatMessageService.saveUserMessage(state);

        // 5.保存AI回复消息
        ChatMessage aiMessage = chatMessageService.saveAiMessage(state);

        // 6.清理Mysql中的会话消息（Mysql保存最近的N条消息）
        chatMessageService.cleanupOldMessageInMysql(dto.getUserId());

        // 7.创建同步任务
        messageSyncTaskService.createSyncTask(state, userMessage, aiMessage);

        // 8.事务提交之后，异步同步会话消息到Milvus
        registerPostCommitHook(sessionId);

        long duration = System.currentTimeMillis() - startTime;
        log.info("【售后客服工作流】执行完成，workflowExecutionId: {}, 耗时: {}ms", executionId, duration);

        return workflowExecutionBuild.buildSuccessResponse(state, executionId, duration);
    }

    /**
     * 执行工作流逻辑
     */
    private CustomerServiceStateDto executeWorkflowInternal(String executionId, String sessionId, WorkflowRequestDto dto) {
        try {
            Map<String, Object> initialState = new HashMap<>();
            initialState.put(WORKFLOW_EXECUTION_ID, executionId);
            initialState.put(SESSION_ID, sessionId);
            initialState.put(USER_ID, dto.getUserId());
            initialState.put(USER_NAME, dto.getUserName());
            initialState.put(USER_INPUT, dto.getUserInput());

            OverAllState overAllState = new OverAllState(initialState);
            RunnableConfig config = RunnableConfig.builder().build();

            Map<String, Object> output = customerServiceGraph.invoke(overAllState, config)
                    .map(OverAllState::data)
                    .orElseThrow(() -> new RuntimeException("【售后客服工作流】执行未返回结果"));

            return customerServiceStateBuild.buildCustomerServiceState(executionId, sessionId, dto, output);
        } catch (Exception e) {
            log.error("【售后客服工作流】工作流执行失败", e);
            throw new RuntimeException("【售后客服工作流】工作流执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存成功执行记录
     */
    private void saveWorkflowExecution(CustomerServiceStateDto state) {
        WorkflowExecution execution = workflowExecutionBuild.createBaseWorkflowExecution(state.getExecutionId(), state.getSessionId(), state.getUserId(), SUCCESS.getId());
        execution.setDurationMs(state.getDuration());
        execution.setInputData(workflowExecutionBuild.buildInputDataJson(state.getUserInput(), state.getUserId(), state.getUserName(), state.getSessionId()));
        execution.setOutputData(workflowExecutionBuild.buildOutputDataJson(state));
        workflowExecutionMapper.insert(execution);
    }

    /**
     * 保存失败执行记录
     */
    private void saveFailureRecord(String executionId, String sessionId, WorkflowRequestDto dto, Exception e) {
        WorkflowExecution execution = workflowExecutionBuild.createBaseWorkflowExecution(executionId, sessionId, dto.getUserId(), WorkflowExecutionStatusEnum.FAILED.getId());
        execution.setInputData(workflowExecutionBuild.buildInputDataJson(dto.getUserInput(), dto.getUserId(), dto.getUserName(), sessionId));
        execution.setErrorMessage(e.getMessage());
        workflowExecutionMapper.insert(execution);
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
                        asyncProcessSyncTask(sessionId); //生产环境推送到MQ消息
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
     * 查询Milvus用户会话消息
     */
    private List<MessageDocumentVO> queryMilvusByUserId(Long userId, int currentPage, int pageSize) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .filterExpression("userId == '" + userId + "'")
                    .build();

            // 执行搜索
            List<Document> documents = customerKnowledgeVectorStore.similaritySearch(searchRequest);

            // 手动排序
            return documents.stream()
                    .sorted((d1, d2) -> {
                        // 按时间倒序排序
                        LocalDateTime t1 = (LocalDateTime) d1.getMetadata().get(CREATE_TIME);
                        LocalDateTime t2 = (LocalDateTime) d2.getMetadata().get(CREATE_TIME);
                        return t2.compareTo(t1);
                    })
                    .skip((currentPage - 1) * pageSize)
                    .limit(pageSize)
                    .map(e -> workflowExecutionBuild.convertToMessageDocumentVO(e))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询Milvus数据失败", e);
            return Collections.emptyList();
        }
    }

}
