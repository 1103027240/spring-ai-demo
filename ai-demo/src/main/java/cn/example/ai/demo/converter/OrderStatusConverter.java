package cn.example.ai.demo.converter;

import cn.hutool.core.util.StrUtil;
import java.util.HashMap;
import java.util.Map;
import static cn.example.ai.demo.enums.OrderStatusEnum.*;

public class OrderStatusConverter {

    // 状态描述到代码的映射
    private static final Map<String, Integer> STATUS_MAPPING = new HashMap<>();

    static {
        // 数字代码映射
        STATUS_MAPPING.put("0", PENDING_PAYMENT.getId());
        STATUS_MAPPING.put("1", PAID.getId());
        STATUS_MAPPING.put("2", SHIPPED.getId());
        STATUS_MAPPING.put("3", COMPLETED.getId());
        STATUS_MAPPING.put("4", CANCELED.getId());
        STATUS_MAPPING.put("5", REFUNDING.getId());
        STATUS_MAPPING.put("6", REFUNDED.getId());

        // 中文状态映射到代码
        STATUS_MAPPING.put("待支付", PENDING_PAYMENT.getId());
        STATUS_MAPPING.put("未支付", PENDING_PAYMENT.getId());
        STATUS_MAPPING.put("等待支付", PENDING_PAYMENT.getId());
        STATUS_MAPPING.put("已支付", PAID.getId());
        STATUS_MAPPING.put("支付成功", PAID.getId());
        STATUS_MAPPING.put("已付款", PAID.getId());
        STATUS_MAPPING.put("已发货", SHIPPED.getId());
        STATUS_MAPPING.put("发货中", SHIPPED.getId());
        STATUS_MAPPING.put("已出库", SHIPPED.getId());
        STATUS_MAPPING.put("已完成", COMPLETED.getId());
        STATUS_MAPPING.put("已签收", COMPLETED.getId());
        STATUS_MAPPING.put("交易完成", COMPLETED.getId());
        STATUS_MAPPING.put("已取消", CANCELED.getId());
        STATUS_MAPPING.put("订单取消", CANCELED.getId());
        STATUS_MAPPING.put("已取消的", CANCELED.getId());
        STATUS_MAPPING.put("退款中", REFUNDING.getId());
        STATUS_MAPPING.put("正在退款", REFUNDING.getId());
        STATUS_MAPPING.put("退款处理中", REFUNDING.getId());
        STATUS_MAPPING.put("已退款", REFUNDED.getId());
        STATUS_MAPPING.put("退款完成", REFUNDED.getId());

        // 英文状态映射（兼容旧版）
        STATUS_MAPPING.put("pending", PENDING_PAYMENT.getId());
        STATUS_MAPPING.put("paid", PAID.getId());
        STATUS_MAPPING.put("shipped", SHIPPED.getId());
        STATUS_MAPPING.put("completed", COMPLETED.getId());
        STATUS_MAPPING.put("delivered", COMPLETED.getId());  // 映射已完成
        STATUS_MAPPING.put("cancelled", CANCELED.getId());
        STATUS_MAPPING.put("refunding", REFUNDING.getId());
        STATUS_MAPPING.put("refunded", REFUNDED.getId());
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

}
