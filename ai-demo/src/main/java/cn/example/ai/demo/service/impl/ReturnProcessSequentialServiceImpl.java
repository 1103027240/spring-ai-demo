package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.build.MultiAgentBuild;
import cn.example.ai.demo.service.ReturnProcessSequentialService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

@Service
public class ReturnProcessSequentialServiceImpl implements ReturnProcessSequentialService {

    @Resource(name = "returnProcessSequentialAgent")
    private SequentialAgent returnProcessSequentialAgent;

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Override
    public Map<String, Object> runSequential(String orderId) throws GraphRunnerException {
        OverAllState overAllState = returnProcessSequentialAgent.invoke(initMap(orderId)).orElse(null);
        if (overAllState == null || CollUtil.isEmpty(overAllState.data())) {
            return Map.of("status", "fail", "orderId", orderId, "msg", "智能体未返回结果");
        }

        Map<String, Object> dataMap = overAllState.data();
        return Map.of(
                "status", "success",
                "orderId", orderId,
                "orderCheckResult", multiAgentBuild.extractText(dataMap, "orderCheckResult"),
                "policyCheckResult", multiAgentBuild.extractText(dataMap, "policyCheckResult"),
                "refundAmount", multiAgentBuild.extractText(dataMap, "refundAmount"),
                "returnOrder", multiAgentBuild.extractText(dataMap, "returnOrder"));
    }

    private Map<String, Object> initMap(String orderId) {
        int status = new SecureRandom().nextInt(3);
        int amount = new SecureRandom().nextInt(1000) + 1;
        long days = new SecureRandom().nextLong(11);

        return Map.of(
                "orderId", orderId,
                "status", (status == 0 ? "Paid" : status == 1 ? "Shipped" : "Delivered"),
                "amount", amount,
                "createTime", DateUtil.format(LocalDateTime.now().plusDays(days), NORM_DATETIME_PATTERN));
    }

}
