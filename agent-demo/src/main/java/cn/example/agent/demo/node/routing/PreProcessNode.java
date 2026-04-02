package cn.example.agent.demo.node.routing;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import static cn.example.agent.demo.constant.FieldConstant.QUERY;
import static cn.example.agent.demo.constant.FieldConstant.USER_INPUT;

@Slf4j
@Component
public class PreProcessNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【PreProcessNode】开始执行...");
        String query = state.value(QUERY, "");
        return Map.of(QUERY, query);
    }

}
