package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.param.dto.ArticleDto;
import cn.example.agent.demo.param.vo.ArticleVO;
import cn.example.agent.demo.service.AgentStructuredDataService;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class AgentStructuredDataServiceImpl implements AgentStructuredDataService {

    private static final String INSTRUCTION = """
        你是一个专业作家。
        1. 请严格按照输入的 topic（主题）、wordCount（字数）和 style（风格）要求创作文章。
        2. 请创作文章并返回包含 title（标题）、content（内容） 和 characterCount（字数） 的结构化结果
        """;

    private static final String INPUT_SCHEMA = """
        {
            "type": "object",
            "required": ["topic", "wordCount", "style"]
            "properties": {
                "topic": {
                    "type": "string"
                },
                "wordCount": {
                    "type": "integer"
                },
                "style": {
                    "type": "string"
                }
            }
        }
        """;

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Override
    public String doChat(String message) {
        ReactAgent agent = ReactAgent.builder()
                .name("结构化输入输出智能体")
                .model(qwenChatModel)
                .instruction(INSTRUCTION)
                .inputType(ArticleDto.class)
                .inputSchema(INPUT_SCHEMA)
                .outputType(ArticleVO.class)
                .outputSchema(new BeanOutputConverter<>(ArticleVO.class).getJsonSchema())
                .build();

        Map<String, Object> map = Map.of(
                "top", message,
                "wordCount", "100",
                "style", "简洁易懂");

        try {
            return agent.call(map).getText();
        } catch (GraphRunnerException e) {
            log.error("结构化输入输出智能体对话执行报错", e);
            throw new RuntimeException(e);
        }
    }

}
