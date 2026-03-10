package cn.getech.base.demo.node.customer;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * 回复生成节点
 * @author 11030
 */
@Slf4j
@Component
public class ResponseGenerationNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("开始回复生成节点");
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        // 从状态中收集所有信息
        String userInput = state.value("userInput", String.class).orElse("");
        String intentRecognition = state.value("intentRecognition", String.class).orElse("generalQuestion");
        String sentimentAnalysis = state.value("sentimentAnalysis", String.class).orElse("neutral");
        String knowledgeContext = state.value("knowledgeContext", String.class).orElse("");
        Object orderResults = state.value("orderResults", Object.class).orElse(null);
        Object afterSalesResult = state.value("afterSalesResult", Object.class).orElse(null);

        // 构建回复生成提示词
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个专业的电商客服助手，请根据以下信息生成回复：\n\n");
        promptBuilder.append("用户输入：").append(userInput).append("\n\n");
        promptBuilder.append("用户意图：").append(intentRecognition).append("\n");
        promptBuilder.append("情感倾向：").append(sentimentAnalysis).append("\n\n");
        if (!knowledgeContext.isEmpty()) {
            promptBuilder.append("相关知识库信息：\n").append(knowledgeContext).append("\n");
        }
        if (orderResults != null) {
            promptBuilder.append("订单查询结果：").append(orderResults).append("\n");
        }
        if (afterSalesResult != null) {
            promptBuilder.append("售后处理结果：").append(afterSalesResult).append("\n");
        }
        promptBuilder.append("\n请生成专业、友好、有帮助的客服回复，注意：\n");
        promptBuilder.append("1. 根据情感倾向调整语气（积极时热情，消极时安抚）\n");
        promptBuilder.append("2. 充分利用相关知识库信息\n");
        promptBuilder.append("3. 如果涉及订单或售后，提供具体信息\n");
        promptBuilder.append("4. 回复要简洁明了，不超过200字\n");

        // 调用大模型
        String response = qwenChatClient.prompt()
                .user(promptBuilder.toString())
                .call()
                .content()
                .trim();
        log.info("生成的回复: {}", response);

        Map<String, Object> result = new HashMap<>();
        result.put("aiResponse", response);
        result.put("responseGenerationTime", System.currentTimeMillis());

        return result;
    }

}
