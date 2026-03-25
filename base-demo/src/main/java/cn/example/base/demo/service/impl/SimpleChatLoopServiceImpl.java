package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.ChatMessageBuild;
import cn.example.base.demo.build.MultiAgentBuild;
import cn.example.base.demo.service.ChatMessageService;
import cn.example.base.demo.service.SimpleChatLoopService;
import cn.example.base.demo.utils.ParamUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static cn.example.base.demo.constant.FieldValueConstant.DEFAULT_LIMIT;

@Slf4j
@Service
public class SimpleChatLoopServiceImpl implements SimpleChatLoopService {

    @Resource(name = "simpleChatLoopAgent")
    private LoopAgent simpleChatLoopAgent;

    @Autowired
    private ChatMessageBuild chatMessageBuild;

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Autowired
    private ChatMessageService chatMessageService;

    @Override
    public Map<String, Object> runLoop(Long userId, String sessionId, String message) throws GraphRunnerException {
        // 1. 查询历史对话
        String conversationHistory = chatMessageBuild.buildChatMessageHistory(userId, sessionId, DEFAULT_LIMIT);

        // 2. 调用多智能体
        OverAllState overAllState = simpleChatLoopAgent.invoke(initMap(userId, sessionId, message, conversationHistory)).orElse(null);
        if (overAllState == null || CollUtil.isEmpty(overAllState.data())) {
            return Map.of("status", "fail", "userId", userId, "sessionId", sessionId,"msg", "智能体未返回结果");
        }

        Map<String, Object> dataMap = overAllState.data();

        // 3. 提取AI回复
        String aiResponse = extractText(dataMap, "agentResponse");

        // 4. 保存用户对话和AI回复消息
        chatMessageService.batchSaveMessages(userId, sessionId, message, aiResponse);

        // 5. 返回AI回复消息
        return Map.of(
                "status", "success",
                "userId", userId,
                "sessionId", sessionId,
                "userMessage", message,
                "aiResponse", aiResponse);
    }

    private Map<String, Object> initMap(Long userId, String sessionId, String userMessage, String conversationHistory) {
        return Map.of(
                "userId", userId,
                "sessionId", sessionId,
                "userMessage", userMessage,
                "conversationHistory", conversationHistory);
    }

    /**
     * 从JSON字符串中提取response字段
     */
    private String extractText(Map<String, Object> dataMap, String key) {
        String text = multiAgentBuild.extractText(dataMap, key);
        if (StrUtil.isBlank(text)) {
            return text;
        }

        // 去除可能的markdown代码块标记
        String jsonStr = ParamUtils.cleanMarkdownCodeBlock(text);
        try {
            JSONObject json = JSONUtil.parseObj(jsonStr);
            return json.getStr("response", text);
        } catch (Exception e) {
            Pattern pattern = Pattern.compile("\"response\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(jsonStr);
            if (matcher.find()) {
                return matcher.group(1);
            }
            log.warn("JSON解析失败，返回原始文本: {}", text);
            return text;
        }
    }

}
