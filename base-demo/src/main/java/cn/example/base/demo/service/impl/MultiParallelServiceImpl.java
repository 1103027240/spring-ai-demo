package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.CustomerVerificationParallelService;
import cn.example.base.demo.service.MultiParallelService;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.agentscope.core.studio.StudioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class MultiParallelServiceImpl implements MultiParallelService {

    @Autowired
    private CustomerVerificationParallelService customerVerificationParallelService;

    @Override
    public Map<String, Object> doChat(String customerId) {
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("并行多智能体")
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();

        try {
            return customerVerificationParallelService.runParallel(customerId);
        } catch (GraphRunnerException e) {
            return Map.of("status", "error", "customerId", customerId, "msg", e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

}
