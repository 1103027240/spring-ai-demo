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
public enum IntentRecognitionEnum {

    ORDER_QUERY("order_query", "订单查询", "订单查询（如：我的订单状态、物流信息）"),

    PRODUCT_INFO("product_info", "商品信息", "商品信息（如：产品规格、价格、库存）"),

    AFTER_SALES("after_sales", "售后服务", "售后服务（如：退货、换货、维修）"),

    PAYMENT_ISSUE("payment_issue", "支付问题", "支付问题（如：支付失败、退款）"),

    LOGISTICS_QUERY("logistics_query", "物流查询", "物流查询（如：快递状态、配送时间）"),

    POLICY_QUESTION("policy_question", "政策咨询", "政策咨询（如：退货政策、保修政策）"),

    COMPLAINT("complaint", "投诉建议", "投诉建议"),

    GENERAL_QUESTION("general_question", "一般咨询", "一般咨询"),

    ;

    private String id;

    private String description;

    private String detailDescription;

}
