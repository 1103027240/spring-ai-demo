package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.MultiLoopService;
import cn.example.agent.demo.service.SimpleCustomerChatLoopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class MultiLoopServiceImpl implements MultiLoopService {

    @Autowired
    private SimpleCustomerChatLoopService simpleCustomerChatLoopService;

    @Override
    public Map<String, Object> doChat(String message, Long userId, String sessionId) {
        try {
            return simpleCustomerChatLoopService.runLoop(userId, sessionId, message);
        } catch (Exception e) {
            log.error("【循环多智能体】执行失败", e);
            return Map.of("status", "error","userId", userId,"sessionId", sessionId,"msg", e.getMessage());
        }
    }

}
