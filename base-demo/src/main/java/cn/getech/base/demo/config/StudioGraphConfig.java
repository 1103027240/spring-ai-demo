package cn.getech.base.demo.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.DocumentReviewNodeEnum.*;

/**
 * Studio可视化配置，用于展示和管理Graph工作流
 * @author 11030
 */
@Slf4j
@Configuration
public class StudioGraphConfig {

    /**
     * 注册文档审批工作流到 Studio
     */
    @Bean
    @DependsOn({"documentReviewGraph", "documentReviewGraphRepresentation"})
    public Map<String, Object> studioWorkflowRegistry(
            CompiledGraph documentReviewGraph,
            GraphRepresentation documentReviewGraphRepresentation) throws GraphStateException {
        Map<String, Object> workflowRegistry = new HashMap<>();

        // 注册工作流
        workflowRegistry.put("document_review", Map.of(
                "name", "文档审批工作流",
                "description", "完整的文档智能审批流程，包括内容分析、合规检查、风险评估和人工审批",
                "graph", documentReviewGraph,
                "representation", documentReviewGraphRepresentation,
                "nodes", new String[]{
                        CONTENT_ANALYSIS.getName(),      // 内容分析
                        COMPLIANCE_CHECK.getName(),      // 合规检查
                        RISK_ASSESSMENT.getName(),       // 风险评估
                        HUMAN_APPROVAL.getName(),        // 人工审批
                        APPROVE_PROCESSING.getName(),    // 通过处理
                        REJECT_PROCESSING.getName(),     // 拒绝处理
                        FINAL_REPORT.getName()           // 最终报告
                },
                "interruptNodes", new String[]{HUMAN_APPROVAL.getName()},
                "version", "1.0.0"));

        return workflowRegistry;
    }

    /**
     * 提供Graph的PlantUML文本（可选，用于外部工具渲染）
     */
    @Bean
    public String documentReviewPlantUml(GraphRepresentation documentReviewGraphRepresentation) {
        String plantUmlContent = documentReviewGraphRepresentation.content();
        log.info("PlantUML可视化内容:\n{}", plantUmlContent);
        return plantUmlContent;
    }

}
