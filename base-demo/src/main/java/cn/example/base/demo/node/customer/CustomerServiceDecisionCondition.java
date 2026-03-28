package cn.example.base.demo.node.customer;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import static cn.example.base.demo.constant.FieldConstant.INTENT;
import static cn.example.base.demo.constant.FieldConstant.SENTIMENT;
import static cn.example.base.demo.enums.CustomerServiceNodeEnum.*;
import static cn.example.base.demo.enums.IntentRecognitionEnum.GENERAL_QUESTION;
import static cn.example.base.demo.enums.SentimentAnalysisEnum.*;

/**
 * 路由决策（条件边）
 * @author 11030
 */
@Slf4j
@Component
public class CustomerServiceDecisionCondition implements EdgeAction {

    @Override
    public String apply(OverAllState state) {
        log.info("【售后客服路由决策】开始执行");

        String intent = state.value(INTENT, String.class).orElse(GENERAL_QUESTION.getId());
        String sentiment = state.value(SENTIMENT, String.class).orElse(NEUTRAL.getId());

        // 根据意图和情感决定路由决策
        String conditionalValue = determineRouteDecision(intent, sentiment);
        log.info("【售后客服路由决策】: intent = {}, sentiment = {}, conditionalValue = {}", intent, sentiment, conditionalValue);
        return conditionalValue;
    }

    private String determineRouteDecision(String intent, String sentiment) {
        switch (intent) {
            case "order_query":
                return ORDER_QUERY.getId();  // 订单查询路由
            case "after_sales":
            case "complaint":
                return AFTER_SALES.getId();  // 售后处理路由
            case "product_info":
            case "policy_question":
            case "general_question":
                return KNOWLEDGE_RETRIEVAL.getId();  // 知识库检索路由
            case "payment_issue":
            case "logistics_query":
                if (Arrays.asList(URGENT.getId(), NEGATIVE.getId()).contains(sentiment)) {
                    return AFTER_SALES.getId();  // 紧急或负面情绪转售后处理
                } else {
                    return KNOWLEDGE_RETRIEVAL.getId();  // 其他情况走知识库检索
                }
            default:
                return KNOWLEDGE_RETRIEVAL.getId();  // 默认知识库检索
        }
    }

}
