package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.Order;
import cn.getech.base.demo.enums.OrderStatusEnum;
import cn.getech.base.demo.mapper.OrderMapper;
import cn.getech.base.demo.service.OrderService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 11030
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    // 订单号正则表达式
    private Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{3,6}\\d{6,12})|(\\d{6,12})");

    /**
     * 查询订单
     */
    @Override
    public List<Map<String, Object>> queryOrders(Map<String, Object> queryParams) {
        List<Map<String, Object>> results = new ArrayList<>();

        // 提取订单号
        String orderNumber = extractOrderNumber(queryParams);
        if (StrUtil.isNotBlank(orderNumber)) {
            Order order = orderMapper.selectByOrderNumber(orderNumber);
            if (order != null) {
                results.add(convertOrderToMap(order));
            }
        }

        // 如果没有指定订单号，查询用户最近的订单
        if (CollUtil.isEmpty(results) && queryParams.containsKey("userId")) {
            Long userId = (Long) queryParams.get("userId");
            List<Order> orders = orderMapper.selectRecentOrdersByUserId(userId, 5);
            results.addAll(orders.stream()
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList()));
        }

        return results;
    }

    /**
     * 获取用户订单统计
     */
    @Override
    public Map<String, Object> getUserOrderStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        int totalOrders = orderMapper.countByUserId(userId);
        int pendingOrders = orderMapper.countByUserIdAndStatus(userId, 0);
        int paidOrders = orderMapper.countByUserIdAndStatus(userId, 1);
        int shippedOrders = orderMapper.countByUserIdAndStatus(userId, 2);
        int completedOrders = orderMapper.countByUserIdAndStatus(userId, 3);

        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("paidOrders", paidOrders);
        stats.put("shippedOrders", shippedOrders);
        stats.put("completedOrders", completedOrders);

        return stats;
    }

    @Override
    public Order getByOrderId(String orderNumber) {
        return orderMapper.getByOrderId(orderNumber);
    }

    /**
     * 从查询参数中提取订单号
     */
    private String extractOrderNumber(Map<String, Object> queryParams) {
        // 1. 从orderNumber字段提取
        if (queryParams.containsKey("orderNumber")) {
            Object orderNumberObj = queryParams.get("orderNumber");
            if (orderNumberObj != null && StrUtil.isNotBlank(orderNumberObj.toString())) {
                return orderNumberObj.toString().trim();
            }
        }

        // 2. 从userInput字段正则匹配
        if (queryParams.containsKey("userInput")) {
            String userInput = (String) queryParams.get("userInput");
            Matcher matcher = ORDER_NUMBER_PATTERN.matcher(userInput);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        return null;
    }

    /**
     * 转换订单实体为Map
     */
    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("userId", order.getUserId());
        result.put("userName", order.getUserName());
        result.put("totalAmount", order.getTotalAmount());
        result.put("status", OrderStatusEnum.getDescription(order.getStatus()));
        result.put("statusCode", order.getStatus());
        result.put("paymentMethod", order.getPaymentMethod());
        result.put("shippingAddress", order.getShippingAddress());
        result.put("contactPhone", order.getContactPhone());
        result.put("createTime", order.getCreateTime());
        result.put("statusDescription", OrderStatusEnum.getDetailDescription(order.getStatus()));
        return result;
    }

}
