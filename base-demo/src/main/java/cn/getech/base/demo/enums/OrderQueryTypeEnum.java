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
public enum OrderQueryTypeEnum {

    BY_ORDER_NUMBER("按订单号精确查询"),

    BY_USER_RECENT("按用户ID查询"),

    BY_STATUS("按订单状态查询"),

    BY_TIME_RANGE( "按时间范围查询"),

    BY_PRODUCT("按商品关键词查询"),

    COMPLEX_QUERY("组合条件查询"),

    ;

    private String text;

}
