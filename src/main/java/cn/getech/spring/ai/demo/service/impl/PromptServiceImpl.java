package cn.getech.spring.ai.demo.service.impl;

import cn.getech.spring.ai.demo.entity.StudentRecord;
import cn.getech.spring.ai.demo.properties.TemplateProperties;
import cn.getech.spring.ai.demo.service.PromptService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.Map;

/**
 * @author 11030
 */
@Service
public class PromptServiceImpl implements PromptService {

    @Resource(name = "qwenChatClient")
    private ChatClient qwenChatClient;

    @Autowired
    private TemplateProperties templateProperties;

    @Override
    public Flux<String> doChatRoleSystem(String msg) {
        PromptTemplate promptTemplate = new PromptTemplate(templateProperties.getRoleSystemTemplate());
        Prompt prompt = promptTemplate.create();
        return qwenChatClient.prompt(prompt).user(msg).stream().content();
    }

    @Override
    public Flux<String> doChatRoleTool(String msg) {
        // AI大模型查询
        PromptTemplate promptTemplate = new PromptTemplate(templateProperties.getRoleAssistantTemplate());
        Prompt prompt = promptTemplate.create(Map.of("msg", msg));
        Flux<String> aiResponse = qwenChatClient.prompt(prompt).stream().content();

        // Tool工具查询
        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(Collections.singletonList(new ToolResponseMessage.ToolResponse("1", "获取天气", msg)))
                .build();
        return aiResponse.concatWith(Mono.just(toolResponseMessage.getText()));
    }

    @Override
    public Flux<String> doChatRoleCombine(String msg) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(templateProperties.getRoleCombineSystemTemplate());
        Message systemMessage = systemPromptTemplate.createMessage();

        PromptTemplate userPromptTemplate = new PromptTemplate(templateProperties.getRoleCombineUserTemplate());
        Message userMessage = userPromptTemplate.createMessage(Map.of("msg", msg));

        Prompt prompt = new Prompt(systemMessage, userMessage);
        return qwenChatClient.prompt(prompt).stream().content();
    }

    @Override
    public StudentRecord doChatOutput(String name, String id, Integer age, String email) {
        return qwenChatClient.prompt()
                .user(promptUserSpec ->
                        promptUserSpec.text(templateProperties.getDemoStudentTemplate())
                                .param("name", name)
                                .param("id", id)
                                .param("age", age)
                                .param("email", email))
                .call()
                .entity(StudentRecord.class);
    }

}
