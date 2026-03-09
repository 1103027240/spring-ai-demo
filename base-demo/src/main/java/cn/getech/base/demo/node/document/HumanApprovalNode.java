package cn.getech.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 人工审批节点（中断节点）
 * 关键：此节点配置了 interrupt: true，流程会在此暂停
 * @author 11030
 */
@Component
@Slf4j
public class HumanApprovalNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String documentContent = state.value("document_content", "");
        String riskLevel = (String) state.value("risk_level").orElse("UNKNOWN");

        // 从状态中获取恢复时传入的审批决策，通过documentReviewGraph.updateState(config, updates)获取的
        String decision = (String) state.value("approval_decision").orElse("PENDING");
        if ("PENDING".equals(decision)) {
            // 理论上不会进入这里，因为中断恢复时必须传入决策。
            log.info("审批决策仍为PENDING，流程可能未正确恢复。");
            return Map.of("approval_output", "WAITING", "next_node", "human_approval");
        }

        // 返回审批结果，将由 ApprovalDecisionRouter 路由到下一个节点
        return Map.of("approval_output", decision);
    }

}
