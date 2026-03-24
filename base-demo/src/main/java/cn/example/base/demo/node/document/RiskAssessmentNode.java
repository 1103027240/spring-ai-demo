package cn.example.base.demo.node.document;

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
        String riskLevel = "MEDIUM"; // HIGH, MEDIUM, LOW
        String riskAssessmentResult = String.format("风险评估完成：风险等级[%s]，建议人工审批。", riskLevel);

        return Map.of(
                "risk_level", riskLevel,
                "risk_assessment_result", riskAssessmentResult);
    }

}
