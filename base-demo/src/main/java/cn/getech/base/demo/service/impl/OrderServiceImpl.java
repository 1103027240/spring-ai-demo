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
import static cn.getech.base.demo.constant.FieldValueConstant.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

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
        if (CollUtil.isEmpty(results) && queryParams.containsKey(USER_ID)) {
            Long userId = (Long) queryParams.get(USER_ID);
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
        if (queryParams.containsKey(ORDER_NUMBER)) {
            Object orderNumberObj = queryParams.get(ORDER_NUMBER);
            if (orderNumberObj != null && StrUtil.isNotBlank(orderNumberObj.toString())) {
                return orderNumberObj.toString().trim();
            }
        }

        // 2. 从userInput字段正则匹配
        if (queryParams.containsKey(USER_INPUT)) {
            String userInput = (String) queryParams.get(USER_INPUT);
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
        result.put(ORDER_ID, order.getId());
        result.put(ORDER_NUMBER, order.getOrderNumber());
        result.put(USER_ID, order.getUserId());
        result.put(USER_NAME, order.getUserName());
        result.put(STATUS, order.getStatus());
        result.put(STATUS_DESCRIPTION, OrderStatusEnum.getDescription(order.getStatus()));
        result.put(STATUS_DETAIL_DESCRIPTION, OrderStatusEnum.getDetailDescription(order.getStatus()));
        result.put(PAYMENT_METHOD, order.getPaymentMethod());
        result.put(TOTAL_AMOUNT, order.getTotalAmount());
        result.put(SHIPPING_ADDRESS, order.getShippingAddress());
        result.put(CONTACT_PHONE, order.getContactPhone());
        result.put(CREATE_TIME, order.getCreateTime());
        return result;
    }

}
