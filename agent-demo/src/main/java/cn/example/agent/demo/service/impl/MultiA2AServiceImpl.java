package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.MultiA2AService;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import static cn.example.agent.demo.constant.FieldConstant.MESSAGE;
import static cn.example.agent.demo.constant.FieldConstant.SUCCESS;

@Service
public class MultiA2AServiceImpl implements MultiA2AService {

    @Autowired
    private AgentCardProvider agentCardProvider;

    @Override
    public Map<String, Object> doChat(String message) {
        A2aRemoteAgent remote = A2aRemoteAgent.builder()
                .name("data_analysis_agent")
                .agentCardProvider(agentCardProvider)
                .description("数据分析远程代理")
                .instruction("{input}")
                .build();

        try {
            OverAllState overAllState = remote.invoke("请根据季度数据给出同比与环比分析概要。").orElse(null);
            if(overAllState == null){
                return Map.of(SUCCESS, false, MESSAGE, "A2A返回结果为空");
            }

            String result = (String) overAllState.data().get("messages");
            return Map.of(SUCCESS, true, MESSAGE, result);
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

}
