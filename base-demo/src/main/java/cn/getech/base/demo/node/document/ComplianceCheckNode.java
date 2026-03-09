package cn.getech.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 合规检查节点
 * @author 11030
 */
@Component
@Slf4j
public class ComplianceCheckNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String contentAnalysisResult = state.value("content_analysis_result", "");

        // 模拟合规检查
        String complianceCheckResult = "合规检查通过：符合公司政策要求。";

        return Map.of(
                "compliance_check_result", complianceCheckResult,
                "next_node", "risk_assessment");
    }

}
