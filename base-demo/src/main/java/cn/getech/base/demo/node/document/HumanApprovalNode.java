package cn.getech.base.demo.node.document;

import cn.getech.base.demo.enums.ApprovalDecisionEnum;
import cn.getech.base.demo.enums.ApprovalStatusEnum;
import cn.getech.base.demo.enums.DocumentReviewNodeEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Map;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.*;

/**
 * 人工审批节点（中断节点）
 * 此节点配置interrupt为true，流程会在此节点前暂停
 * @author 11030
 */
@Component
@Slf4j
public class HumanApprovalNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String riskLevel = state.value("risk_level", "UNKNOWN");
        String riskAssessmentResult = state.value("risk_assessment_result", "");

        // approval_decision通过documentReviewGraph.updateState(config, updates)更新状态，然后从状态中获取更新后的approval_decision
        String approvalDecision = state.value("approval_decision", "PENDING");
        if (Arrays.asList(PENDING.getId(), WAITING.getId()).contains(approvalDecision)) {
            // 理论上不会进入这里，因为中断恢复时审批决策传APPROVE/REJECT
            log.info("审批决策仍为PENDING或WAIT，流程可能未正确从中断恢复。。。");

            return Map.of(
                    "approval_output", ApprovalDecisionEnum.WAITING.getId(), //决策状态为待审批
                    "approval_status", ApprovalStatusEnum.WAITING.getId(), //流程状态为待审批
                    "next_node", DocumentReviewNodeEnum.HUMAN_APPROVAL.getText()); //返回当前人工审批节点
        }

        // 通过ApprovalDecisionRouter路由到下一个节点
        log.info("人工审批节点执行完毕。当前决策为: {}。", approvalDecision);
        return Map.of("approval_output", approvalDecision);
    }

}
