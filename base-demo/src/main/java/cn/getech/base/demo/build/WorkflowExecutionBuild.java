package cn.getech.base.demo.build;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.WorkflowExecution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.constant.FieldValueConstant.ERROR_AI_RESPONSE;
import static cn.getech.base.demo.constant.FieldValueConstant.WORKFLOW_CUSTOMER_SERVICE;

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
        execution.setStartTime(LocalDateTime.now());
        execution.setEndTime(LocalDateTime.now());
        return execution;
    }

    /**
     * 构建输入数据JSON
     */
    public String buildInputDataJson(String userInput, Long userId, String userName, String sessionId)  {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userInput", userInput);
        inputData.put("sessionId", sessionId);
        inputData.put("userId", userId);
        inputData.put("userName", userName);
        inputData.put("timestamp", System.currentTimeMillis());
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
        outputData.put("aiResponse", state.getAiResponse());
        outputData.put("intent", state.getIntent());
        outputData.put("sentiment", state.getSentiment());
        outputData.put("workflowStatus", state.getStatus());
        outputData.put("executionPath", state.getExecutionPath());
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
        response.put("executionId", executionId);
        response.put("status", "success");
        response.put("aiResponse", state.getAiResponse());
        response.put("intent", state.getIntent());
        response.put("sentiment", state.getSentiment());
        response.put("durationMs", duration);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 构建失败响应
     */
    public Map<String, Object> buildErrorResponse(String executionId, Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("executionId", executionId);
        response.put("status", "error");
        response.put("error", e.getMessage());
        response.put("aiResponse", ERROR_AI_RESPONSE);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

}
