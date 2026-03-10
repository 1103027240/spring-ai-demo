package cn.getech.base.demo.node.customer;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.*;

/**
 * 路由决策（条件边）
 * @author 11030
 */
@Slf4j
@Component
public class CustomerServiceDecisionRouter implements EdgeAction {

    @Override
    public String apply(OverAllState state) {
        log.info("开始售后客服路由决策");
        String intentRecognition = state.value("intentRecognition", String.class).orElse("generalQuestion");
        String sentimentAnalysis = state.value("sentimentAnalysis", String.class).orElse("neutral");

        // 根据意图和情感决定路由决策
        String conditionalValue = determineRouteDecision(intentRecognition, sentimentAnalysis);
        log.info("售后客服路由决策: intentRecognition = {}, sentimentAnalysis = {}, conditionalValue = {}", intentRecognition, sentimentAnalysis, conditionalValue);
        return conditionalValue;
    }

    private String determineRouteDecision(String intentRecognition, String sentimentAnalysis) {
        switch (intentRecognition) {
            case "order_query":
                return ORDER_QUERY.getId();
            case "after_sales":
                return AFTER_SALES.getId();
            case "payment_issue":
            case "logistics_query":
                if (Arrays.asList("urgent", "negative").contains(sentimentAnalysis)) {
                    return AFTER_SALES.getId(); // 紧急或负面情绪转售后处理
                }
                return KNOWLEDGE_RETRIEVAL.getId();
            case "complaint":
                return AFTER_SALES.getId(); // 投诉转售后处理
            default:
                return KNOWLEDGE_RETRIEVAL.getId();
        }
    }

}
