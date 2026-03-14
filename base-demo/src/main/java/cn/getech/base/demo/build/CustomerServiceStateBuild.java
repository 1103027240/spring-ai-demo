package cn.getech.base.demo.build;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.dto.WorkflowRequestDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.constant.FieldConstant.AFTER_SALES_RESULT;
import static cn.getech.base.demo.constant.FieldConstant.ORDER_RESULTS;
import static cn.getech.base.demo.constant.FieldValueConstant.DEFAULT_AI_RESPONSE;
import static cn.getech.base.demo.constant.PatternConstant.AI_RESPONSE_KEYS;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.AFTER_SALES;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.ORDER_QUERY;
import static cn.getech.base.demo.enums.WorkflowExecutionStatusEnum.SUCCESS;

@Component
public class CustomerServiceStateBuild {

    /**
     * 构建客服状态DTO
     */
    public CustomerServiceStateDto buildCustomerServiceState(String executionId, String sessionId, WorkflowRequestDto dto, Map<String, Object> output) {
        CustomerServiceStateDto state = new CustomerServiceStateDto(executionId, sessionId, dto.getUserInput(), dto.getUserId(), dto.getUserName(), System.currentTimeMillis());

        state.setIntent((String) output.get(INTENT));
        state.setSentiment((String) output.get(SENTIMENT));
        state.setKnowledgeContext((String) output.get(KNOWLEDGE_CONTEXT));

        if (output.containsKey(ORDER_RESULTS)) {
            List<Map<String, Object>> orderResults = (List<Map<String, Object>>) output.get(ORDER_RESULTS);
            state.recordNodeResult(ORDER_QUERY.getId(), Map.of(RESULTS, orderResults));
        }

        if (output.containsKey(AFTER_SALES_RESULT)) {
            Map<String, Object> afterSalesResult = (Map<String, Object>) output.get(AFTER_SALES_RESULT);
            state.recordNodeResult(AFTER_SALES.getId(), Map.of(RESULT, afterSalesResult));
        }

        String aiResponse = extractAiResponse(output);
        state.complete(SUCCESS.getId(), aiResponse);

        return state;
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

}
