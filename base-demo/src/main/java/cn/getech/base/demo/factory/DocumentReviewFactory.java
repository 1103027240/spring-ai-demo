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
            strategies.put("approval_decision", new ReplaceStrategy()); // 审批决定
            strategies.put("final_action", new ReplaceStrategy());

            return strategies;
        };
    }

}
