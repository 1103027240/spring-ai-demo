package cn.getech.base.demo.factory;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 11030
 */
public class DocumentReviewFactory {

    public static KeyStrategyFactory documentReviewKeyStrategyFactory() {
        return () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();

            // 核心业务数据使用替换策略
            strategies.put("document_content", new ReplaceStrategy());
            strategies.put("content_analysis_result", new ReplaceStrategy());
            strategies.put("compliance_check_result", new ReplaceStrategy());
            strategies.put("risk_assessment_result", new ReplaceStrategy());
            strategies.put("risk_level", new ReplaceStrategy());
            strategies.put("approval_decision", new ReplaceStrategy());
            strategies.put("approval_output", new ReplaceStrategy());
            strategies.put("processing_result", new ReplaceStrategy());
            strategies.put("final_report", new ReplaceStrategy());
            strategies.put("approval_status", new ReplaceStrategy());

            return strategies;
        };
    }

}
