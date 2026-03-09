package cn.getech.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 风险评估节点
 * @author 11030
 */
@Component
@Slf4j
public class RiskAssessmentNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String complianceCheckResult = state.value("compliance_check_result", "");

        // 模拟风险评估
        String riskResult = "风险评估：中等风险，建议人工审批";

        return Map.of(
                "risk_assessment_result", riskResult,
                "next_node", "human_approval");
    }

}
