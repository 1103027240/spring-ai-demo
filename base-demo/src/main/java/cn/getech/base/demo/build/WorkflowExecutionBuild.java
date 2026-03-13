package cn.getech.base.demo.build;

import cn.getech.base.demo.constant.FieldValueConstant;
import cn.getech.base.demo.converter.DocumentConverter;
import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.dto.MessageDocumentVO;
import cn.getech.base.demo.entity.WorkflowExecution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.constant.FieldValueConstant.*;

@Component
public class WorkflowExecutionBuild {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建基础工作流执行记录
     */
    public WorkflowExecution createBaseWorkflowExecution(String executionId, String sessionId, Long userId, String status) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setExecutionId(executionId);
        execution.setWorkflowName(WORKFLOW_CUSTOMER_SERVICE);
        execution.setSessionId(sessionId);
        execution.setUserId(userId);
        execution.setStatus(status);
        execution.setCreateTime(LocalDateTime.now());
        execution.setStartTime(LocalDateTime.now());
        execution.setEndTime(LocalDateTime.now());
        return execution;
    }

    /**
     * 构建输入数据JSON
     */
    public String buildInputDataJson(String userInput, Long userId, String userName, String sessionId)  {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(USER_INPUT, userInput);
        inputData.put(SESSION_ID, sessionId);
        inputData.put(USER_ID, userId);
        inputData.put(USER_NAME, userName);
        inputData.put(TIME_STAMP, System.currentTimeMillis());
        try {
            return objectMapper.writeValueAsString(inputData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建输出数据JSON
     */
    public String buildOutputDataJson(CustomerServiceStateDto state) {
        Map<String, Object> outputData = new HashMap<>();
        outputData.put(AI_RESPONSE, state.getAiResponse());
        outputData.put(INTENT, state.getIntent());
        outputData.put(SENTIMENT, state.getSentiment());
        outputData.put(WORKFLOW_STATUS, state.getStatus());
        outputData.put(EXECUTION_PATH, state.getExecutionPath());
        try {
            return objectMapper.writeValueAsString(outputData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建成功响应
     */
    public Map<String, Object> buildSuccessResponse(CustomerServiceStateDto state, String executionId, long duration) {
        Map<String, Object> response = new HashMap<>();
        response.put(WORKFLOW_EXECUTION_ID, executionId);
        response.put(AI_RESPONSE, state.getAiResponse());
        response.put(INTENT, state.getIntent());
        response.put(SENTIMENT, state.getSentiment());
        response.put(DURATION_MS, duration);
        response.put(TIME_STAMP, System.currentTimeMillis());
        return response;
    }

    /**
     * 转换为MessageDocument
     */
    public MessageDocumentVO convertToMessageDocumentVO(Document doc) {
        return DocumentConverter.toEntity(doc, (content, metadata) ->
                MessageDocumentVO.builder()
                        .id(doc.getId())
                        .content(doc.getText())
                        .metadata(metadata)
                        .messageId((Double) metadata.get(FieldValueConstant.MESSAGE_ID))
                        .userId((Double) metadata.get(FieldValueConstant.USER_ID))
                        .sessionId((String) metadata.get(FieldValueConstant.SESSION_ID))
                        .messageType((Double) metadata.get(FieldValueConstant.MESSAGE_TYPE))
                        .workflowExecutionId((String) metadata.get(FieldValueConstant.WORKFLOW_EXECUTION_ID))
                        .createTime((Double) metadata.get(FieldValueConstant.CREATE_TIME))
                        .build());
    }

}
