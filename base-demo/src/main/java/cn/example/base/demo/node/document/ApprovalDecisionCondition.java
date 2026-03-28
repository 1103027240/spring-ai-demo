package cn.example.base.demo.node.document;

import cn.example.base.demo.enums.ApprovalDecisionEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static cn.example.base.demo.enums.ApprovalDecisionEnum.APPROVE;
import static cn.example.base.demo.enums.ApprovalDecisionEnum.REJECT;

/**
 * 路由决策（条件边）
 * @author 11030
 */
@Component
@Slf4j
public class ApprovalDecisionCondition implements EdgeAction {

    @Override
    public String apply(OverAllState state) {
        String approvalOutput = state.value("approval_output", String.class)
                .orElseThrow(() -> new IllegalStateException("未找到审批决策"));
        log.info("审批决策分发: decision = {}", approvalOutput);

        // 返回条件值
        String conditionalValue = determineRouteDecision(approvalOutput);
        log.info("文档审批路由决策: conditionalValue = {}", conditionalValue);
        return conditionalValue;
    }

    private String determineRouteDecision(String approvalOutput) {
        ApprovalDecisionEnum approvalOutputEnum = ApprovalDecisionEnum.valueOf(approvalOutput);
        return switch (approvalOutputEnum) {
            case APPROVE -> APPROVE.getId();
            case REJECT -> REJECT.getId();
            default -> throw new IllegalArgumentException("无效的审批决策: " + approvalOutput);
        };
    }

}
