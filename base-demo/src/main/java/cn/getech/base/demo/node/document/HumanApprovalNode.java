package cn.getech.base.demo.node.document;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;

/**
 * 人工审批节点（中断节点）
 * 关键：此节点配置了 interrupt: true，流程会在此暂停
 * @author 11030
 */
@Component
@Slf4j
public class HumanApprovalNode implements NodeActionWithConfig, InterruptableAction {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
//        // 获取之前的分析结果
//        String content = state.value("document_content", "");
//        String riskAssessmentResult = state.value("risk_assessment_result", "");
//
//        log.info("人工审批节点：等待审批决策");
//        log.info("文档摘要: {}", content);
//        log.info("风险评估: {}", riskAssessmentResult);
//
//        // 这里不返回审批决定，因为决定将由外部输入（resume时传入）
//        return Map.of(
//                "approval_status", "PENDING",
//                "message", "等待人工审批决策");

        // 检查是否有审批决定（恢复时传入）
        String approvalDecision = state.value("approval_decision", "");
        if (StrUtil.isNotBlank(approvalDecision)) {
            // 恢复执行，已有审批决定
            log.info("人工审批节点：收到审批决定 = {}", approvalDecision);

            String comment = state.value("approval_comment", "");
            String approver = state.value("approver", "");

            return Map.of(
                    "approval_decision", approvalDecision,
                    "approval_comment", comment,
                    "approver", approver,
                    "approval_status", "COMPLETED"
            );
        } else {
            // 首次执行，等待审批
            String content = state.value("document_content", "");
            String riskAssessmentResult = state.value("risk_assessment_result", "");

            log.info("人工审批节点：等待审批决策");
            log.info("文档摘要：{}", content);
            log.info("风险评估：{}", riskAssessmentResult);

            return Map.of(
                    "approval_status", "PENDING",
                    "message", "等待人工审批决策");
        }
    }

    /**
     * 提供中断元数据
     */
    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 构建中断信息，用于前端展示
        InterruptionMetadata metadata = InterruptionMetadata.builder(nodeId, state)
                .addMetadata("message", "文档需要人工审批，请查看详情并做出决定")
                .addMetadata("document_summary", state.value("document_content", ""))
                .addMetadata("risk_level", "MEDIUM")
                .addMetadata("required_action", "APPROVE_OR_REJECT")
                .build();
        return Optional.of(metadata);
    }

}
