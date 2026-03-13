package cn.getech.base.demo.node.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.getech.base.demo.enums.IntentRecognitionEnum.GENERAL_QUESTION;
import static cn.getech.base.demo.enums.SentimentAnalysisEnum.NEUTRAL;

/**
 * 回复生成节点
 * @author 11030
 */
@Slf4j
@Component
public class ResponseGenerationNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("【回复生成节点】开始执行");

        // 从状态中收集所有信息
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");
        String userInput = state.value(USER_INPUT, String.class).orElse("");
        String intent = state.value(INTENT, String.class).orElse(GENERAL_QUESTION.getId());
        String sentiment = state.value(SENTIMENT, String.class).orElse(NEUTRAL.getId());
        String knowledgeContext = state.value(KNOWLEDGE_CONTEXT, String.class).orElse("");
        List<Map<String, Object>> orderResults = state.value(ORDER_RESULTS, List.class).orElse(null);
        Map<String, Object> afterSalesResult = state.value(AFTER_SALES_RESULT, Map.class).orElse(null);

        // 构建回复生成提示词
        String responsePrompt = buildResponsePrompt(userInput, intent, sentiment, knowledgeContext, orderResults, afterSalesResult);

        // 调用大模型
        String response = qwenChatClient.prompt()
                .user(responsePrompt)
                .call()
                .content()
                .trim();
        log.info("【回复生成节点】AI生成回复: {}", response);

        Map<String, Object> result = new HashMap<>();
        result.put(AI_RESPONSE, response);
        result.put(RESPONSE_GENERATION_TIME, System.currentTimeMillis());
        return result;
    }

    /**
     * 构建回复生成提示词
     */
    private String buildResponsePrompt(String userInput, String intent, String sentiment,
                                       String knowledgeContext, List<Map<String, Object>> orderResults,
                                       Map<String, Object> afterSalesResult) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("你是一个专业的电商客服助手，请根据以下信息生成回复：\n\n");
        promptBuilder.append("用户输入：").append(userInput).append("\n\n");
        promptBuilder.append("用户意图：").append(intent).append("\n");
        promptBuilder.append("情感倾向：").append(sentiment).append("\n\n");

        if (StrUtil.isNotBlank(knowledgeContext)) {
            promptBuilder.append("相关知识库信息：\n").append(knowledgeContext).append("\n");
        }
        if (CollUtil.isNotEmpty(orderResults)) {
            promptBuilder.append("订单查询结果：").append(orderResults).append("\n");
        }
        if (CollUtil.isNotEmpty(afterSalesResult)) {
            promptBuilder.append("售后处理结果：").append(afterSalesResult).append("\n");
        }

        promptBuilder.append("\n请生成专业、友好、有帮助的客服回复，注意：\n");
        promptBuilder.append("1. 根据情感倾向调整语气（积极时热情，消极时安抚，紧急时快速响应）\n");
        promptBuilder.append("2. 充分利用相关知识库信息\n");
        promptBuilder.append("3. 如果涉及订单或售后，提供具体信息\n");
        promptBuilder.append("4. 回复要简洁明了，不超过200字\n");
        promptBuilder.append("5. 如果用户提供了具体信息（如订单号），请在回复中确认\n");
        promptBuilder.append("6. 给出明确的后续步骤或解决方案\n");
        promptBuilder.append("7. 使用中文进行回复\n");

        return promptBuilder.toString();
    }

}
