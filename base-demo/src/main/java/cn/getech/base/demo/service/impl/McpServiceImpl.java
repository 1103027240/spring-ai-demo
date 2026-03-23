package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.McpService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author 11030
 */
@Service
public class McpServiceImpl implements McpService {

//    @Resource(name = "mcpQwenChatClient")
//    private ChatClient mcpQwenChatClient;

    /**
     * 修改mcp-server.json中百度地图密钥
     */
    @Override
    public Flux<String> doChat(String msg) {
        //return mcpQwenChatClient.prompt(msg).stream().content();
        return null;
    }

}
