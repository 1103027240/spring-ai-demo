package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.McpService;
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
