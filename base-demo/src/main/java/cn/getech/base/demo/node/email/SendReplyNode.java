package cn.getech.base.demo.node.email;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.Map;

/**
 * 发送回复节点
 * @author 11030
 */
public class SendReplyNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String draftResponse = state.value("draft_response", "");

        // 与邮件服务集成

        return Map.of("status", "sent", "draft_response", draftResponse);
    }

}
