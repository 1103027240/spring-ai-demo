package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.StudioBuild;
import cn.example.base.demo.service.CustomerVerificationParallelService;
import cn.example.base.demo.service.MultiParallelService;
import io.agentscope.core.studio.StudioManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class MultiParallelServiceImpl implements MultiParallelService {

    @Autowired
    private CustomerVerificationParallelService customerVerificationParallelService;

    @Autowired
    private StudioBuild studioBuild;

    @Override
    public Map<String, Object> doChat(String customerId) {
        try {
            studioBuild.initStudio("并行多智能体");
            return customerVerificationParallelService.runParallel(customerId);
        } catch (Exception e) {
            log.error("【并行多智能体】执行失败", e);
            return Map.of("status", "error", "customerId", customerId, "msg", e.getMessage());
        } finally {
            StudioManager.shutdown();
        }
    }

}
