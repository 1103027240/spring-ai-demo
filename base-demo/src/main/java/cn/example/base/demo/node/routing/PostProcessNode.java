package cn.example.base.demo.node.routing;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.flow.node.RoutingMergeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.FINAL_RESULT;

@Slf4j
@Component
public class PostProcessNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String result = state.value(RoutingMergeNode.DEFAULT_MERGED_OUTPUT_KEY, "");
        return Map.of(FINAL_RESULT, result);
    }

}
