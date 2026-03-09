package cn.getech.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 审批决策分发器（条件边逻辑）
 * @author 11030
 */
@Component
@Slf4j
public class ApprovalDecisionCondition implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        String decision = (String) state.value("approval_decision")
                .orElseThrow(() -> new IllegalStateException("未找到审批决定"));
        log.info("审批决策分发: decision = {}", decision);

        return switch (decision) {
            case "APPROVE" -> "approve_processing";
            case "REJECT" -> "reject_processing";
            default -> throw new IllegalArgumentException("无效的审批决定: " + decision);
        };
    }

}
