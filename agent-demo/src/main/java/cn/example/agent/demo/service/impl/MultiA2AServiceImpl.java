package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.MultiA2AService;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
                .description("数据分析远程代理")
                .agentCardProvider(agentCardProvider)
                .instruction("{input}")
                .build();

        try {
            OverAllState overAllState = remote.invoke(message).orElse(null);
            if(overAllState == null){
                return Map.of(SUCCESS, false, MESSAGE, "A2A返回结果为空");
            }

            String result = (String) overAllState.data().get("output"); //返回数据从output获取
            return Map.of(SUCCESS, true, MESSAGE, result);
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

}
