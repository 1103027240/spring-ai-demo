package cn.example.mcp.client.demo.service.impl;

import cn.example.mcp.client.demo.service.McpDemoService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * @author 11030
 */
@Service
public class McpDemoServiceImpl implements McpDemoService {

    @Resource(name = "toolQwenChatClient")
    private ChatClient toolQwenChatClient;

    @Override
    public String doChat(String msg) {
        return toolQwenChatClient.prompt().user(msg).call().content();
    }

}
