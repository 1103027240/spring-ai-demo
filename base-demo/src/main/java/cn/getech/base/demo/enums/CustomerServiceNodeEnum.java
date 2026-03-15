package cn.getech.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CustomerServiceNodeEnum {

    INTENT_RECOGNITION("intent_recognition", "意图识别"),

    SENTIMENT_ANALYSIS("sentiment_analysis", "情感分析"),

    KNOWLEDGE_RETRIEVAL("knowledge_retrieval", "知识库检索"),

    ORDER_QUERY("order_query", "订单查询"),

    AFTER_SALES("after_sales", "售后处理"),

    RESPONSE_GENERATION("response_generation", "回复生成"),

    ;

    private String id;

    private String text;

}
