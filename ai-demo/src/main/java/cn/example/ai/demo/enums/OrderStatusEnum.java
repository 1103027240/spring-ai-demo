package cn.example.ai.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderStatusEnum {

    PENDING_PAYMENT(0, "待支付", "订单已创建，等待支付"),

    PAID(1, "已支付", "支付成功，等待发货"),

    SHIPPED(2, "已发货", "商品已发货，运输中"),

    COMPLETED(3, "已完成", "订单已完成，感谢购买"),

    CANCELED(4, "已取消", "订单已取消"),

    REFUNDING(5, "退款中", "退款申请处理中"),

    REFUNDED(6, "已退款", "退款已完成"),

    ;

    private Integer id;

    private String text;

    private String detailText;

    public static String getText(Integer id) {
        return Arrays.asList(OrderStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.id, id))
                .findFirst()
                .map(OrderStatusEnum::getText)
                .orElse("未知状态");
    }

    public static String getDetailText(Integer id) {
        return Arrays.asList(OrderStatusEnum.values())
                .stream().filter(e -> Objects.equals(e.id, id))
                .findFirst()
                .map(OrderStatusEnum::getDetailText)
                .orElse("");
    }

}
