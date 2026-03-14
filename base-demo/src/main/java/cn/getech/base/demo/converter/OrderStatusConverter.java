package cn.getech.base.demo.converter;

import cn.hutool.core.util.StrUtil;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.OrderStatusEnum.*;

public class OrderStatusConverter {

    // 状态描述到代码的映射
    private static final Map<String, Integer> STATUS_MAPPING = new HashMap<>();

    static {
        // 数字代码映射
        STATUS_MAPPING.put("0", PENDING_PAYMENT.getCode());
        STATUS_MAPPING.put("1", PAID.getCode());
        STATUS_MAPPING.put("2", SHIPPED.getCode());
        STATUS_MAPPING.put("3", COMPLETED.getCode());
        STATUS_MAPPING.put("4", CANCELED.getCode());
        STATUS_MAPPING.put("5", REFUNDING.getCode());
        STATUS_MAPPING.put("6", REFUNDED.getCode());

        // 中文状态映射到代码
        STATUS_MAPPING.put("待支付", PENDING_PAYMENT.getCode());
        STATUS_MAPPING.put("未支付", PENDING_PAYMENT.getCode());
        STATUS_MAPPING.put("等待支付", PENDING_PAYMENT.getCode());
        STATUS_MAPPING.put("已支付", PAID.getCode());
        STATUS_MAPPING.put("支付成功", PAID.getCode());
        STATUS_MAPPING.put("已付款", PAID.getCode());
        STATUS_MAPPING.put("已发货", SHIPPED.getCode());
        STATUS_MAPPING.put("发货中", SHIPPED.getCode());
        STATUS_MAPPING.put("已出库", SHIPPED.getCode());
        STATUS_MAPPING.put("已完成", COMPLETED.getCode());
        STATUS_MAPPING.put("已签收", COMPLETED.getCode());
        STATUS_MAPPING.put("交易完成", COMPLETED.getCode());
        STATUS_MAPPING.put("已取消", CANCELED.getCode());
        STATUS_MAPPING.put("订单取消", CANCELED.getCode());
        STATUS_MAPPING.put("已取消的", CANCELED.getCode());
        STATUS_MAPPING.put("退款中", REFUNDING.getCode());
        STATUS_MAPPING.put("正在退款", REFUNDING.getCode());
        STATUS_MAPPING.put("退款处理中", REFUNDING.getCode());
        STATUS_MAPPING.put("已退款", REFUNDED.getCode());
        STATUS_MAPPING.put("退款完成", REFUNDED.getCode());

        // 英文状态映射（兼容旧版）
        STATUS_MAPPING.put("pending", PENDING_PAYMENT.getCode());
        STATUS_MAPPING.put("paid", PAID.getCode());
        STATUS_MAPPING.put("shipped", SHIPPED.getCode());
        STATUS_MAPPING.put("completed", COMPLETED.getCode());
        STATUS_MAPPING.put("delivered", COMPLETED.getCode());  // 映射到"已完成"
        STATUS_MAPPING.put("cancelled", CANCELED.getCode());
        STATUS_MAPPING.put("refunding", REFUNDING.getCode());
        STATUS_MAPPING.put("refunded", REFUNDED.getCode());
    }

    /**
     * 将状态字符串转换为状态代码
     */
    public static Integer convertToStatusCode(String statusStr) {
        if (StrUtil.isBlank(statusStr) || statusStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = statusStr.trim();

        // 1. 如果是数字，直接解析
        try {
            int code = Integer.parseInt(trimmed);
            if (code >= 0 && code <= 6) {
                return code;
            }
        } catch (NumberFormatException e) {
            // 不是数字，继续处理
        }

        // 2. 从映射表中查找
        if (STATUS_MAPPING.containsKey(trimmed.toLowerCase())) {
            return STATUS_MAPPING.get(trimmed.toLowerCase());
        }

        // 3. 尝试模糊匹配
        for (Map.Entry<String, Integer> entry : STATUS_MAPPING.entrySet()) {
            if (trimmed.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 获取状态描述
     */
    public static String getStatusDescription(Integer statusCode) {
        if (statusCode == null) {
            return "未知状态";
        }

        switch (statusCode) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "退款中";
            case 6: return "已退款";
            default: return "未知状态(" + statusCode + ")";
        }
    }

    /**
     * 获取详细状态描述
     */
    public static String getStatusDetailDescription(Integer statusCode) {
        if (statusCode == null) {
            return "订单状态未知";
        }

        switch (statusCode) {
            case 0: return "订单已创建，等待支付";
            case 1: return "支付成功，等待发货";
            case 2: return "商品已发货，运输中";
            case 3: return "订单已完成，感谢购买";
            case 4: return "订单已取消";
            case 5: return "退款申请处理中";
            case 6: return "退款已完成";
            default: return "订单状态未知";
        }
    }

}
