package cn.example.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SqlQueryTypeEnum {

    SALES_ANALYSIS("SALES_ANALYSIS", "最近7天销售额"),

    RECENT_ORDERS("RECENT_ORDERS", "最近7天订单信息"),

    TOP_PRODUCTS("TOP_PRODUCTS", "热门商品/畅销商品"),

    CUSTOMER_ANALYSIS("CUSTOMER_ANALYSIS", "客户分析/客户消费"),

    PAYMENT_INFO("PAYMENT_INFO", "支付信息"),

    INVENTORY_INFO("INVENTORY_INFO", "库存信息"),

    GENERAL_QUERY("GENERAL_QUERY", "默认查询"),

    ;

    private String id;

    private String text;

}
