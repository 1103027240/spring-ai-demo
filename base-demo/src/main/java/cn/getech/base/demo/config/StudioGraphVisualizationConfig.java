package cn.getech.base.demo.config;

import com.alibaba.cloud.ai.graph.StateGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import static cn.getech.base.demo.contant.WorkFlowTitleConstant.DOCUMENT_REVIEW_NAME;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.APPROVE;
import static cn.getech.base.demo.enums.ApprovalDecisionEnum.REJECT;
import static cn.getech.base.demo.enums.DocumentReviewNodeEnum.*;

/**
 * Graph可视化视图启动监听器，在应用启动时输出Graph的可视化信息
 * @author 11030
 */
@Slf4j
@Configuration
public class StudioGraphVisualizationConfig {

    @Value("${spring.ai.studio.enabled:false}")
    private boolean studioEnabled;

    /**
     * 应用启动完成后，输出Graph可视化信息
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (studioEnabled) {
            log.info("\n" + "=".repeat(80));
            log.info("🎨 Document Review Graph 可视化已配置");
            log.info("=".repeat(80));
            log.info("Graph 工作流：{}", DOCUMENT_REVIEW_NAME);
            log.info("");
            log.info("📋 节点流程:");
            log.info("  {} → {} → {} → {} → {} (中断点)", StateGraph.START, CONTENT_ANALYSIS.getName(), COMPLIANCE_CHECK.getName(), RISK_ASSESSMENT.getName(), HUMAN_APPROVAL.getName());
            log.info("                               ↓");
            log.info("                    ({}) ← 审批决策 → ({})", APPROVE.getDescription(), REJECT.getDescription());
            log.info("                      ↓                 ↓");
            log.info("                   通过处理           拒绝处理");
            log.info("                      ↓                 ↓");
            log.info("                        └─→ {} ←─┘", FINAL_REPORT.getName());
            log.info("                               ↓");
            log.info("                            {}", StateGraph.END);
            log.info("");
            log.info("💡 提示:");
            log.info("  - 工作流会在'人工审批'节点前自动中断");
            log.info("  - 调用 resume 接口可恢复工作流执行");
            log.info("  - PlantUML 流程图已输出到日志");
            log.info("=".repeat(80) + "\n");
        } else {
            log.warn("⚠️Studio 未启用，如需启用请设置 spring.ai.studio.enabled=true");
        }
    }

}
