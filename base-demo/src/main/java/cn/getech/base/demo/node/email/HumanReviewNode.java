package cn.getech.base.demo.node.email;

import cn.getech.base.demo.dto.EmailClassifyDto;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.Map;

/**
 * 人工审核节点
 * @author 11030
 */
public class HumanReviewNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        EmailClassifyDto emailClassifyDto = state.value("emailClassify")
                .map(v -> (EmailClassifyDto) v)
                .orElse(new EmailClassifyDto());

        // 准备审核数据
        Map<String, Object> reviewData = Map.of(
                "email_id", state.value("email_id", ""),
                "original_email", state.value("email_content", ""),
                "draft_response", state.value("draft_response", ""),
                "intent", emailClassifyDto.getIntent(),
                "urgency", emailClassifyDto.getUrgency(),
                "action", "请审核并批准/编辑此响应"
        );

        // 在 interruptBefore 模式下，此节点在人工输入后才会执行，返回审核数据和下一个节点
        return Map.of(
                "review_data", reviewData,
                "status", "waiting_for_review",
                "next_node", "send_reply"
        );

    }

}
