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
import java.time.LocalDateTime;
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
     * 1. 按订单号精确查询
     * 2. 按用户ID查询
     * 3. 按订单状态查询
     * 4. 按时间范围查询
     * 5. 按商品关键词查询
     * 6. 组合条件查询
     */
    @Override
    public List<Map<String, Object>> queryOrders(Map<String, Object> queryParams) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // 1. 首先尝试按订单号精确查询（最高优先级）
            String orderNumber = extractOrderNumber(queryParams);
            if (StrUtil.isNotBlank(orderNumber)) {
                log.info("【订单查询】按订单号查询: {}", orderNumber);
                Order order = orderMapper.selectByOrderNumber(orderNumber);
                if (order != null) {
                    // 验证用户权限（如果提供了用户ID）
                    if (queryParams.containsKey(USER_ID)) {
                        Long userId = (Long) queryParams.get(USER_ID);
                        if (!userId.equals(order.getUserId())) {
                            log.warn("【订单查询】订单不属于当前用户，订单用户ID: {}，当前用户ID: {}", order.getUserId(), userId);
                            return results; // 返回空列表
                        }
                    }
                    results.add(convertOrderToMap(order));
                    log.info("【订单查询】按订单号查询成功，找到1个订单");
                    return results;
                } else {
                    log.info("【订单查询】未找到订单号: {}", orderNumber);
                    return results; // 返回空列表
                }
            }

            // 2. 获取用户ID（如果存在）
            Long userId = null;
            if (queryParams.containsKey(USER_ID)) {
                userId = (Long) queryParams.get(USER_ID);
            }

            if (userId == null) {
                log.warn("【订单查询】用户ID为空，无法进行用户订单查询");
                return results;
            }

            // 3. 构建用户ID条件
            Map<String, Object> condition = new HashMap<>();
            condition.put(USER_ID, userId);

            // 4. 添加订单状态条件
            if (queryParams.containsKey(STATUS) && StrUtil.isNotBlank(queryParams.get(STATUS).toString())) {
                String status = queryParams.get(STATUS).toString();
                condition.put(STATUS, status);
                log.debug("【订单查询】添加状态条件: {}", status);
            }

            // 5. 添加时间范围条件
            if (queryParams.containsKey(START_TIME) && queryParams.get(START_TIME) != null) {
                condition.put(START_TIME, queryParams.get(START_TIME));
            }
            if (queryParams.containsKey(END_TIME) && queryParams.get(END_TIME) != null) {
                condition.put(END_TIME, queryParams.get(END_TIME));
            }

            // 设置默认时间范围（最近30天）
            if (!condition.containsKey(START_TIME)) {
                LocalDateTime startTime = LocalDateTime.now().minusDays(30);
                condition.put(START_TIME, startTime);
            }
            if (!condition.containsKey(END_TIME)) {
                LocalDateTime endTime = LocalDateTime.now();
                condition.put(END_TIME, endTime);
            }

            // 6. 添加商品关键词条件
            if (queryParams.containsKey(PRODUCT_KEYWORD) && StrUtil.isNotBlank(queryParams.get(PRODUCT_KEYWORD).toString())) {
                String productKeyword = queryParams.get(PRODUCT_KEYWORD).toString();
                condition.put(PRODUCT_KEYWORD, productKeyword);
                log.debug("【订单查询】添加商品关键词条件: {}", productKeyword);
            }

            // 7. 添加分页限制
            int limit = 20; // 默认限制20条
            if (queryParams.containsKey(LIMIT) && queryParams.get(LIMIT) != null) {
                try {
                    limit = Integer.parseInt(queryParams.get(LIMIT).toString());
                } catch (NumberFormatException e) {
                    log.warn("【订单查询】limit参数格式错误，使用默认值20");
                }
            }
            condition.put(LIMIT, limit);

            // 8. 添加排序条件
            condition.put(ORDER_BY, "create_time");
            condition.put(ORDER_DESC, "DESC"); // 默认按创建时间倒序

            // 9. 根据查询条件执行查询
            List<Order> orders = new ArrayList<>();

            // 根据条件组合选择不同的查询方法
            if (condition.containsKey(PRODUCT_KEYWORD)) {
                // 按商品关键词查询
                orders = orderMapper.selectByProductKeyword(condition);
            } else if (condition.containsKey(STATUS)) {
                // 按状态查询
                orders = orderMapper.selectByStatusAndTimeRange(condition);
            } else if (condition.containsKey(START_TIME) && condition.containsKey(END_TIME)) {
                // 按时间范围查询
                orders = orderMapper.selectByTimeRange(condition);
            } else {
                // 默认查询用户最近订单
                orders = orderMapper.selectRecentOrdersByUserId(userId, limit);
            }

            if (CollUtil.isNotEmpty(orders)) {
                results = orders.stream().map(this::convertOrderToMap).collect(Collectors.toList());
                log.info("【订单查询】查询成功，找到 {} 个订单", results.size());
            } else {
                log.info("【订单查询】未找到符合条件的订单");
            }

            return results;
        } catch (Exception e) {
            log.error("【订单查询】查询失败，参数: {}", queryParams, e);
            return results; // 返回空列表
        }
    }

    @Override
    public Order getByOrderId(String orderNumber) {
        return orderMapper.getByOrderId(orderNumber);
    }

    /**
     * 从查询参数中提取订单号
     */
    private String extractOrderNumber(Map<String, Object> queryParams) {
        if (CollUtil.isEmpty(queryParams)) {
            return null;
        }

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
            if (StrUtil.isNotBlank(userInput)) {
                Matcher matcher = ORDER_NUMBER_PATTERN.matcher(userInput);
                if (matcher.find()) {
                    return matcher.group();
                }

                // 尝试其他格式的订单号
                Pattern otherPattern = Pattern.compile("(?i)订单[：: ]?\\s*(\\w{8,20})");
                Matcher otherMatcher = otherPattern.matcher(userInput);
                if (otherMatcher.find()) {
                    return otherMatcher.group(1);
                }
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
