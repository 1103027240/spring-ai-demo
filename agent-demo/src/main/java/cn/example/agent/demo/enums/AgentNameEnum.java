package cn.example.agent.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AgentNameEnum {

    RETURN_ORDER_CHECK("RETURN_ORDER_CHECK", "订单验证智能体"),

    RETURN_POLICY_CHECK("RETURN_POLICY_CHECK", "政策检查智能体"),

    REFUND_CALCULATE("REFUND_CALCULATE", "退款计算智能体"),

    RETURN_ORDER_GENERATE("RETURN_ORDER_GENERATE", "退货单生成智能体"),

    RETURN_PROCESS_SEQUENTIAL("RETURN_PROCESS_SEQUENTIAL", "退货处理顺序智能体"),

    CREDIT_SCORE_CHECK("CREDIT_SCORE_CHECK", "信用分检查智能体"),

    ORDER_SUCCESS_RATE("ORDER_SUCCESS_RATE", "订单成功率分析智能体"),

    AVERAGE_ORDER_VALUE("AVERAGE_ORDER_VALUE", "客单价分析智能体"),

    REFUND_RATE_CHECK("REFUND_RATE_CHECK", "退款率分析智能体"),

    CUSTOMER_VERIFICATION_PARALLEL("CUSTOMER_VERIFICATION_PARALLEL", "客户信息核查并行智能体"),

    SIMPLE_CUSTOMER_SERVICE("SIMPLE_CUSTOMER_SERVICE", "简单客服助手智能体"),

    SIMPLE_CUSTOMER_CHAT_LOOP("SIMPLE_CUSTOMER_CHAT_LOOP", "简单客服对话循环智能体"),

    ;

    private String id;

    private String text;


}
