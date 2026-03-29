package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.StudioBuild;
import cn.example.base.demo.service.MultiLoopService;
import cn.example.base.demo.service.SimpleCustomerChatLoopService;
import io.agentscope.core.studio.StudioManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class MultiLoopServiceImpl implements MultiLoopService {

    @Autowired
    private SimpleCustomerChatLoopService simpleCustomerChatLoopService;

    @Autowired
    private StudioBuild studioBuild;

    @Override
    public Map<String, Object> doChat(String message, Long userId, String sessionId) {
        try {
            studioBuild.initStudio("循环多智能体");
            return simpleCustomerChatLoopService.runLoop(userId, sessionId, message);
        } catch (Exception e) {
            log.error("【循环多智能体】执行失败", e);
            return Map.of("status", "error","userId", userId,"sessionId", sessionId,"msg", e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

}
