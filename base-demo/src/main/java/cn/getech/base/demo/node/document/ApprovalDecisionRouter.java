package cn.getech.base.demo.node.document;

import cn.getech.base.demo.enums.ApprovalDecisionEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.APPROVE;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.REJECT;

/**
 * 审批决策分发器（条件边）
 * @author 11030
 */
@Component
@Slf4j
public class ApprovalDecisionRouter implements EdgeAction {

    @Override
    public String apply(OverAllState state) {
        String approvalOutput = (String) state.value("approval_output")
                .orElseThrow(() -> new IllegalStateException("未找到审批决策"));
        log.info("审批决策分发: decision = {}", approvalOutput);

        // 返回审批决策
        ApprovalDecisionEnum approvalOutputEnum = ApprovalDecisionEnum.valueOf(approvalOutput);
        return switch (approvalOutputEnum) {
            case APPROVE -> APPROVE.getId();
            case REJECT -> REJECT.getId();
            default -> throw new IllegalArgumentException("无效的审批决策: " + approvalOutput);
        };
    }

}
