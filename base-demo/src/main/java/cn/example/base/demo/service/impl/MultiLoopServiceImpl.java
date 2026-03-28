package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.MultiLoopService;
import cn.example.base.demo.service.SimpleCustomerChatLoopService;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.agentscope.core.studio.StudioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class MultiLoopServiceImpl implements MultiLoopService {

    @Autowired
    private SimpleCustomerChatLoopService simpleCustomerChatLoopService;

    @Override
    public Map<String, Object> doChat(String message, Long userId, String sessionId) {
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("循环多智能体")
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();

        try {
            return simpleCustomerChatLoopService.runLoop(userId, sessionId, message);
        } catch (GraphRunnerException e) {
            return Map.of("status", "error","userId", userId,"sessionId", sessionId,"msg", e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

}
