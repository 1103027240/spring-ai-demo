package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.CustomerVerificationParallelService;
import cn.example.agent.demo.service.MultiParallelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class MultiParallelServiceImpl implements MultiParallelService {

    @Autowired
    private CustomerVerificationParallelService customerVerificationParallelService;

    @Override
    public Map<String, Object> doChat(String customerId) {
        try {
            return customerVerificationParallelService.runParallel(customerId);
        } catch (Exception e) {
            log.error("【并行多智能体】执行失败", e);
            return Map.of("status", "error", "customerId", customerId, "msg", e.getMessage());
        }
    }

}
