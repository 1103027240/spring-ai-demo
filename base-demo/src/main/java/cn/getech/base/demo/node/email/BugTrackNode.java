package cn.getech.base.demo.node.email;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bug跟踪节点
 * @author 11030
 */
public class BugTrackNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String ticketId = String.format("Bug-", UUID.randomUUID().toString().replace("-", ""));

        return Map.of(
                "search_results", List.of("已创建Bug单据: " + ticketId),
                "next_node", "draft_response"
        );
    }

}
