package cn.example.agent.demo.node.routing;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import static cn.example.agent.demo.constant.FieldConstant.FINAL_RESULT;
import static cn.example.agent.demo.constant.FieldConstant.OUT_RESULT;

@Slf4j
@Component
public class PostProcessNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String result = state.value(OUT_RESULT, String.class).orElse("");
        return Map.of(FINAL_RESULT, result);
    }

}
