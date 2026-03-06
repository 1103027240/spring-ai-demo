package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.DemoService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
@Service
public class DemoServiceImpl implements DemoService {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Resource(name = "deepseekChatModel")
    private ChatModel deepseekChatModel;

    @Resource(name = "qwenChatClient")
    private ChatClient qwenChatClient;

    @Resource(name = "deepseekChatClient")
    private ChatClient deepseekChatClient;

    @Override
    public String doChatModelQwen(String msg) {
        return qwenChatModel.call(msg);
    }

    @Override
    public Flux<String> doChatModelDeepseek(String msg) {
        return deepseekChatModel.stream(msg);
    }

    @Override
    public String doChatClientQwen(String msg) {
        return qwenChatClient.prompt().user(msg).call().content();
    }

    @Override
    public Flux<String> doChatClientDeepseek(String msg) {
        return deepseekChatClient.prompt().user(msg).stream().content();
    }

}
