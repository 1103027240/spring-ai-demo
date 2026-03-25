package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.SimpleChatLoopService;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SimpleChatLoopServiceImpl implements SimpleChatLoopService {

    @Resource(name = "simpleChatLoopAgent")
    private LoopAgent simpleChatLoopAgent;

    @Override
    public Map<String, Object> doChat(String userId, String sessionId, String message) throws GraphRunnerException {
        OverAllState overAllState = simpleChatLoopAgent.invoke(initMap(userId, sessionId, message)).orElse(null);
        if (overAllState == null || CollUtil.isEmpty(overAllState.data())) {
            return null;
        }

        Map<String, Object> dataMap = overAllState.data();
        return Map.of();
    }

    private Map<String, Object> initMap(String userId, String sessionId, String message) {
        return Map.of(
                "userId", userId,
                "sessionId", sessionId,
                "userMessage", message
        );
    }


}
