package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.AgentMultiModalService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Base64Source;
import io.agentscope.core.message.ImageBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@Service
public class AgentMultiModalServiceImpl implements AgentMultiModalService {

    @Resource(name = "qwenMultiModalChatModel")
    private Model qwenMultiModalChatModel;

    @Override
    public String doChat(String message) {
        try {
            TextBlock textBlock = TextBlock.builder()
                    .text(message)
                    .build();

            String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("E:\\images\\1.jpeg")));
            ImageBlock imageBlock = ImageBlock.builder()
                    .source(Base64Source.builder()
                            .mediaType("image/jpeg")
                            .data(base64Image)
                            .build())
                    .build();

            ReActAgent agent = ReActAgent.builder()
                    .name("agentMultiModal")
                    .model(qwenMultiModalChatModel)
                    .build();

            Msg msg = Msg.builder()
                    .content(List.of(textBlock, imageBlock))
                    .build();

            return agent.call(msg).block().getTextContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
