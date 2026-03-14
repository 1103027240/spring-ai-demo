package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.Order;
import cn.getech.base.demo.enums.OrderQueryTypeEnum;
import cn.getech.base.demo.enums.OrderStatusEnum;
import cn.getech.base.demo.mapper.OrderMapper;
import cn.getech.base.demo.service.OrderService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.constant.PatternConstant.ORDER_NUMBER_PATTERN;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

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
        log.info("【订单查询】开始查询，请求参数: {}", queryParams);
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            String queryType = (String) queryParams.get(QUERY_TYPE);
            OrderQueryTypeEnum orderQueryTypeEnum = OrderQueryTypeEnum.valueOf(queryType);
            if (StrUtil.isNotBlank(queryType)) {
                switch (orderQueryTypeEnum) {
                    case BY_ORDER_NUMBER:
                        results = queryByOrderNumber(queryParams);
                        break;
                    case BY_STATUS:
                        results = queryByStatus(queryParams);
                        break;
                    case BY_TIME_RANGE:
                        results = queryByTimeRange(queryParams);
                        break;
                    case BY_PRODUCT:
                        results = queryByProduct(queryParams);
                        break;
                    case COMPLEX_QUERY:
                        results = queryByComplexConditions(queryParams);
                        break;
                    case BY_USER_RECENT:
                    default:
                        results = queryUserRecentOrders(queryParams);
                        break;
                }
            } else {  // 如果没有指定查询类型，使用智能路由
                results = queryOrdersIntelligent(queryParams);
            }
            log.info("【订单查询】查询完成，查询类型: {}, 结果数量: {}", queryType, results.size());
        } catch (Exception e) {
            log.error("【订单查询】查询失败，参数: {}", queryParams, e);
        }
        return results;
    }

    @Override
    public Order getByOrderNumber(String orderNumber) {
        return orderMapper.getByOrderNumber(orderNumber);
    }

    /**
     * 按订单号查询
     */
    public List<Map<String, Object>> queryByOrderNumber(Map<String, Object> queryParams) {
        List<Map<String, Object>> results = new ArrayList<>();

        String orderNumber = extractOrderNumber(queryParams);
        if (StrUtil.isBlank(orderNumber)) {
            log.warn("【按订单号查询】订单号为空");
            return results;
        }

        Order order = orderMapper.getByOrderNumber(orderNumber);
        if (order != null) {
            // 验证用户权限
            Long userId = (Long) queryParams.get(USER_ID);
            if (userId != null && !userId.equals(order.getUserId())) {
                log.warn("【按订单号查询】订单不属于当前用户，订单用户ID: {}，当前用户ID: {}", order.getUserId(), userId);
                return results;
            }
            results.add(convertOrderToMap(order));
        }

        return results;
    }

    /**
     * 按状态查询
     */
    public List<Map<String, Object>> queryByStatus(Map<String, Object> queryParams) {
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, queryParams.get(USER_ID));
        params.put(STATUS, queryParams.get(STATUS));

        // 设置时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);

        if (queryParams.containsKey(START_TIME)) {
            Object startTimeObj = queryParams.get(START_TIME);
            if (startTimeObj instanceof String) {
                startTime = LocalDateTime.parse((String) startTimeObj, NORM_DATETIME_FORMATTER);
            } else if (startTimeObj instanceof LocalDateTime) {
                startTime = (LocalDateTime) startTimeObj;
            }
        }

        if (queryParams.containsKey(END_TIME)) {
            Object endTimeObj = queryParams.get(END_TIME);
            if (endTimeObj instanceof String) {
                endTime = LocalDateTime.parse((String) endTimeObj, NORM_DATETIME_FORMATTER);
            } else if (endTimeObj instanceof LocalDateTime) {
                endTime = (LocalDateTime) endTimeObj;
            }
        }

        params.put(START_TIME, startTime);
        params.put(END_TIME, endTime);
        params.put(LIMIT, queryParams.getOrDefault(LIMIT, 20));

        List<Order> orders = orderMapper.selectByStatusAndTimeRange(params);
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
    }

    /**
     * 按时间范围查询
     */
    public List<Map<String, Object>> queryByTimeRange(Map<String, Object> queryParams) {
        Map<String, Object> params = new HashMap<>();

        if (queryParams.containsKey(USER_ID)) {
            params.put(USER_ID, queryParams.get(USER_ID));
        }

        // 处理时间范围
        if (queryParams.containsKey(START_TIME)) {
            params.put(START_TIME, queryParams.get(START_TIME));
        } else {
            params.put(START_TIME, LocalDateTime.now().minusDays(30));
        }

        if (queryParams.containsKey(END_TIME)) {
            params.put(END_TIME, queryParams.get(END_TIME));
        } else {
            params.put(END_TIME, LocalDateTime.now());
        }

        params.put(LIMIT, queryParams.getOrDefault(LIMIT, 20));

        List<Order> orders = orderMapper.selectByTimeRange(params);
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
    }

    /**
     * 按商品查询
     */
    public List<Map<String, Object>> queryByProduct(Map<String, Object> queryParams) {
        Map<String, Object> params = new HashMap<>();

        if (queryParams.containsKey(USER_ID)) {
            params.put(USER_ID, queryParams.get(USER_ID));
        }

        if (queryParams.containsKey(PRODUCT_KEYWORD)) {
            params.put(PRODUCT_KEYWORD, queryParams.get(PRODUCT_KEYWORD));
        }

        // 设置时间范围
        if (!params.containsKey(START_TIME)) {
            params.put(START_TIME, LocalDateTime.now().minusDays(90)); // 商品查询范围更大
        }
        if (!params.containsKey(END_TIME)) {
            params.put(END_TIME, LocalDateTime.now());
        }

        params.put(LIMIT, queryParams.getOrDefault(LIMIT, 20));

        List<Order> orders = orderMapper.selectByProductKeyword(params);
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
    }

    /**
     * 复合条件查询
     */
    public List<Map<String, Object>> queryByComplexConditions(Map<String, Object> queryParams) {
        Map<String, Object> params = new HashMap<>();

        // 复制相关参数
        String[] paramKeys = {USER_ID, ORDER_NUMBER, STATUS, PRODUCT_KEYWORD, START_TIME, END_TIME, LIMIT};

        for (String key : paramKeys) {
            if (queryParams.containsKey(key) && queryParams.get(key) != null) {
                params.put(key, queryParams.get(key));
            }
        }

        // 设置默认值
        if (!params.containsKey(LIMIT)) {
            params.put(LIMIT, 20);
        }

        if (!params.containsKey(START_TIME)) {
            params.put(START_TIME, LocalDateTime.now().minusDays(30));
        }

        if (!params.containsKey(END_TIME)) {
            params.put(END_TIME, LocalDateTime.now());
        }

        List<Order> orders = orderMapper.selectByCondition(params);
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
    }

    /**
     * 查询用户最近订单
     */
    public List<Map<String, Object>> queryUserRecentOrders(Map<String, Object> queryParams) {
        Long userId = (Long) queryParams.get(USER_ID);
        if (userId == null) {
            log.warn("【按用户最近订单查询】用户ID为空");
            return new ArrayList<>();
        }

        int limit = (int) queryParams.getOrDefault(LIMIT, 5);

        List<Order> orders = orderMapper.selectRecentOrdersByUserId(userId, limit);
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
    }

    /**
     * 智能路由查询订单
     */
    private List<Map<String, Object>> queryOrdersIntelligent(Map<String, Object> queryParams) {
        // 1. 首先尝试按订单号精确查询
        String orderNumber = extractOrderNumber(queryParams);
        if (StrUtil.isNotBlank(orderNumber)) {
            log.debug("【智能查询】按订单号查询: {}", orderNumber);
            return queryByOrderNumber(Collections.singletonMap(ORDER_NUMBER, orderNumber));
        }

        // 2. 检查用户ID
        Long userId = (Long) queryParams.get(USER_ID);
        if (userId == null) {
            log.warn("【智能查询】用户ID为空，无法查询");
            return new ArrayList<>();
        }

        // 3. 构建查询条件
        Map<String, Object> condition = new HashMap<>();
        condition.put(USER_ID, userId);

        // 4. 检查状态条件
        if (queryParams.containsKey(STATUS) && queryParams.get(STATUS) != null) {
            condition.put(STATUS, queryParams.get(STATUS));
        }

        // 5. 检查时间范围
        if (queryParams.containsKey(START_TIME) && queryParams.get(START_TIME) != null) {
            condition.put(START_TIME, queryParams.get(START_TIME));
        }
        if (queryParams.containsKey(END_TIME) && queryParams.get(END_TIME) != null) {
            condition.put(END_TIME, queryParams.get(END_TIME));
        }

        // 6. 检查商品关键词
        if (queryParams.containsKey(PRODUCT_KEYWORD) && StrUtil.isNotBlank(queryParams.get(PRODUCT_KEYWORD).toString())) {
            condition.put(PRODUCT_KEYWORD, queryParams.get(PRODUCT_KEYWORD));
        }

        // 7. 设置默认分页
        if (!condition.containsKey(LIMIT)) {
            condition.put(LIMIT, 20);
        }

        // 8. 执行查询
        List<Order> orders = orderMapper.selectByCondition(condition);
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
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
