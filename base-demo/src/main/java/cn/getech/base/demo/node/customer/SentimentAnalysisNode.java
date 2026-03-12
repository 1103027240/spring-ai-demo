package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.enums.SentimentAnalysisEnum;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.SentimentAnalysisEnum.*;

/**
 * 情感分析节点
 * @author 11030
 */
@Slf4j
@Component
public class SentimentAnalysisNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("【情感分析节点】开始执行");

        String userInput = state.value("userInput", String.class).orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        // 调用大模型进行情感分析
        String sentiment = getSentimentAnalysis(userInput);

        // 计算情感强度
        double sentimentIntensity = calculateSentimentIntensity(userInput, sentiment);
        log.info("【情感分析节点】情感强度：{}", sentimentIntensity);

        Map<String, Object> result = new HashMap<>();
        result.put("sentiment", sentiment);
        result.put("sentimentIntensity", sentimentIntensity);
        result.put("sentimentAnalysisTime", System.currentTimeMillis());
        return result;
    }

    private String getSentimentAnalysis(String userInput){
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        String sentimentPrompt = """
                请分析以下用户输入的情感倾向，从以下类别中选择：
                1. positive - 积极（表达满意、感谢、赞扬等）
                2. neutral - 中性（客观询问、陈述事实等）
                3. negative - 消极（表达不满、抱怨、愤怒等）
                4. urgent - 紧急（表达急切、需要立即处理）
                
                用户输入：%s
                
                请只返回情感类别代码，不要返回其他内容。
                例如：如果用户问"太慢了，怎么还没发货！"，返回urgent
                """.formatted(userInput);

        // 调用大模型进行情感分析
        String sentiment = qwenChatClient.prompt()
                .user(sentimentPrompt)
                .call()
                .content()
                .trim();

        sentiment = validateAndNormalizeSentimentAnalysis(sentiment);
        log.info("【情感分析节点】分析结果: {}", sentiment);
        return sentiment;
    }

    /**
     * 验证和规范化情感
     */
    private String validateAndNormalizeSentimentAnalysis(String sentiment) {
        if (StrUtil.isBlank(sentiment)) {
            return NEUTRAL.getId();
        }

        String normalized = sentiment.trim().toLowerCase();

        // 检查是否是合法的情感
        if (normalized.contains(POSITIVE.getId())) {
            return POSITIVE.getId();
        } else if (normalized.contains(NEUTRAL.getId())) {
            return NEUTRAL.getId();
        } else if (normalized.contains(NEGATIVE.getId())) {
            return NEGATIVE.getId();
        } else if (normalized.contains(URGENT.getId())) {
            return URGENT.getId();
        } else {
            return NEUTRAL.getId();
        }
    }

    /**
     * 计算情感强度
     */
    private double calculateSentimentIntensity(String userInput, String sentiment) {
        if (StrUtil.isBlank(sentiment)) {
            return 0.5;
        }

        String lowerInput = userInput.toLowerCase();
        SentimentAnalysisEnum sentimentAnalysisEnum = valueOf(sentiment);

        switch (sentimentAnalysisEnum) {
            case POSITIVE:
                if (lowerInput.contains("非常") || lowerInput.contains("很") || lowerInput.contains("太")) {
                    return 0.9;
                } else {
                    return 0.7;
                }
            case NEGATIVE:
                if (lowerInput.contains("非常") || lowerInput.contains("很") || lowerInput.contains("太") || lowerInput.contains("极其")) {
                    return 0.9;
                } else if (lowerInput.contains("有点") || lowerInput.contains("稍微")) {
                    return 0.6;
                } else {
                    return 0.8;
                }
            case URGENT:
                if (lowerInput.contains("立刻") || lowerInput.contains("马上") || lowerInput.contains("立即") || lowerInput.contains("赶紧") || lowerInput.contains("快")) {
                    return 0.95;
                } else {
                    return 0.8;
                }
            case NEUTRAL:
            default:
                return 0.5;
        }
    }

}
