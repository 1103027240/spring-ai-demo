package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.AgentDemoService;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentDemoServiceImpl implements AgentDemoService {

    private static final String SYSTEM_PROMPT = """
        你是一个专业的{role}助手。
        你的专业领域是{domain}。
        请用{language}语言回答用户的问题。
        """;

    private static final String INSTRUCTION = """
        用户询问的主题是：{topic}
        请根据以下要求回答：
        1. 保持专业性
        2. 提供具体示例
        3. 语言要{style}
        """;

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Override
    public String doChat(String message) {
        ReactAgent agent = ReactAgent.builder()
                .name("智能体示例")
                .model(qwenChatModel)
                .systemPrompt(SYSTEM_PROMPT)
                .instruction(INSTRUCTION)
                .build();

        Map<String, Object> map = Map.of(
                "role", "技术专家",
                "domain", "JAVA企业级开发",
                "language", "中文",
                "topic", message,
                "style", "简洁易懂");

        try {
            agent.invoke(map).ifPresent(overAllState -> {
                //如果是invoke调用，先从state获取messages(默认)，再判断MessageType（主要是AssistantMessage，可能有其他类型）
                List<Message> messages = overAllState.value("messages", new ArrayList<Message>()).stream()
                        .filter(e -> Objects.equals(MessageType.ASSISTANT, e.getMessageType()))
                        .map(e -> (AssistantMessage) e)
                        .collect(Collectors.toList());
                log.info("messages: {}", JSONObject.toJSONString(messages));
            });

            return agent.call(map).getText();
        } catch (Exception e) {
            log.error("智能体对话执行报错", e);
            throw new RuntimeException(e);
        }
    }

}
