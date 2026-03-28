package cn.example.base.demo.node.routing;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.FINAL_RESULT;
import static cn.example.base.demo.constant.FieldConstant.MERGED_RESULT;

@Slf4j
@Component
public class PostProcessNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String result = state.value(MERGED_RESULT, "");
        return Map.of(FINAL_RESULT, result);
    }

}
