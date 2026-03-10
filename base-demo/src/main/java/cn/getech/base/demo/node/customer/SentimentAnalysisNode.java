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
 * 情感分析节点
 * @author 11030
 */
@Slf4j
@Component
public class SentimentAnalysisNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("开始情感分析节点");
        String userInput = state.value("userInput", String.class)
                .orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        // 构建情感分析提示词
        String sentimentPrompt = """
                请分析以下用户输入的情感倾向，从以下类别中选择：
                1. positive - 积极（表达满意、感谢、赞扬等）
                2. neutral - 中性（客观询问、陈述事实等）
                3. negative - 消极（表达不满、抱怨、愤怒等）
                4. urgent - 紧急（表达急切、需要立即处理）
                
                用户输入：%s
                
                请只返回情感类别代码，不要返回其他内容。
                """.formatted(userInput);

        // 调用大模型进行情感分析
        String sentiment = qwenChatClient.prompt()
                .user(sentimentPrompt)
                .call()
                .content()
                .trim();
        log.info("情感分析结果: {}", sentiment);

        Map<String, Object> result = new HashMap<>();
        result.put("sentimentAnalysis", sentiment);
        result.put("sentimentAnalysisTime", System.currentTimeMillis());

        return result;
    }

}
