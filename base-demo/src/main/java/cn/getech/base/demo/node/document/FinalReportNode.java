package cn.getech.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 最终报告节点
 */
@Component
@Slf4j
public class FinalReportNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) {
        log.info("生成最终报告...");

        // 汇总所有结果
        String report = String.format(
            "审批流程最终报告：\n" +
            "1. 内容分析: %s\n" +
            "2. 合规检查: %s\n" +
            "3. 风险评估: %s\n" +
            "4. 审批决定: %s\n" +
            "5. 处理结果: %s",
                state.value("content_analysis_result").orElse(""),
                state.value("compliance_check_result").orElse(""),
                state.value("risk_assessment_result").orElse(""),
                state.value("approval_output").orElse(""),
                state.value("processing_result").orElse(""));

        return Map.of(
                "final_report", report,
                "approval_status", "COMPLETED");
    }

}