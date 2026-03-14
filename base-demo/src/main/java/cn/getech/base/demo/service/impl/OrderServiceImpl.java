package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.Order;
import cn.getech.base.demo.enums.OrderQueryTypeEnum;
import cn.getech.base.demo.enums.OrderStatusEnum;
import cn.getech.base.demo.mapper.OrderMapper;
import cn.getech.base.demo.service.OrderService;
import cn.getech.base.demo.utils.ParamUtils;
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
import static cn.getech.base.demo.constant.FieldValueConstant.*;
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
        String queryType = ParamUtils.getQueryParam(queryParams, QUERY_TYPE, String.class);

        try {
            return executeQueryByType(queryType, queryParams);
        } catch (IllegalArgumentException e) {
            log.warn("【订单查询】查询类型参数无效: {}, 使用智能路由", queryType);
            return queryOrdersIntelligent(queryParams);
        } catch (Exception e) {
            log.error("【订单查询】查询失败，参数: {}, 耗时: {}ms", queryParams, e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据查询类型执行查询
     */
    private List<Map<String, Object>> executeQueryByType(String queryType, Map<String, Object> queryParams) {
        if (StrUtil.isBlank(queryType)) {
            return queryOrdersIntelligent(queryParams);
        }

        OrderQueryTypeEnum orderQueryTypeEnum = ParamUtils.parseEnum(OrderQueryTypeEnum.class, queryType);
        return switch (orderQueryTypeEnum) {
            case BY_ORDER_NUMBER -> queryByOrderNumber(queryParams);
            case BY_STATUS -> queryByStatus(queryParams);
            case BY_TIME_RANGE -> queryByTimeRange(queryParams);
            case BY_PRODUCT -> queryByProduct(queryParams);
            case COMPLEX_QUERY -> queryByComplexConditions(queryParams);
            default -> queryUserRecentOrders(queryParams);
        };
    }

    @Override
    public Order getByOrderNumber(String orderNumber) {
        return orderMapper.getByOrderNumber(orderNumber);
    }

    /**
     * 按订单号查询
     */
    public List<Map<String, Object>> queryByOrderNumber(Map<String, Object> queryParams) {
        String orderNumber = extractOrderNumber(queryParams);
        if (StrUtil.isBlank(orderNumber)) {
            log.warn("【按订单号查询】订单号为空");
            return Collections.emptyList();
        }

        return Optional.ofNullable(orderMapper.getByOrderNumber(orderNumber))
                .filter(order -> hasOrderAccessPermission(order, queryParams))
                .map(order -> Collections.singletonList(convertOrderToMap(order)))
                .orElseGet(() -> {
                    log.debug("【按订单号查询】订单不存在: {}", orderNumber);
                    return Collections.emptyList();
                });
    }

    /**
     * 按状态查询
     */
    public List<Map<String, Object>> queryByStatus(Map<String, Object> queryParams) {
        Map<String, Object> params = new HashMap<>();

        // 复制相关参数
        String[] paramKeys = {USER_ID, STATUS, ORDER_BY, ORDER_DESC, LIMIT};
        ParamUtils.copyValidParams(queryParams, params, paramKeys);

        // 处理时间范围
        //setDefaultTimeRange(params, queryParams, DEFAULT_QUERY_DAYS);

        // 设置分页
        params.put(LIMIT, queryParams.getOrDefault(LIMIT, DEFAULT_LIMIT));

        return convertOrders(orderMapper.selectByStatusAndTimeRange(params));
    }

    /**
     * 按时间范围查询
     */
    public List<Map<String, Object>> queryByTimeRange(Map<String, Object> queryParams) {
        Map<String, Object> params = new HashMap<>();

        // 复制相关参数
        String[] paramKeys = {USER_ID, START_TIME, END_TIME, ORDER_BY, ORDER_DESC, LIMIT};
        ParamUtils.copyValidParams(queryParams, params, paramKeys);

        // 处理时间范围
        setDefaultTimeRange(params, queryParams, DEFAULT_QUERY_DAYS);

        // 设置分页
        params.put(LIMIT, queryParams.getOrDefault(LIMIT, DEFAULT_LIMIT));

        return convertOrders(orderMapper.selectByTimeRange(params));
    }

    /**
     * 按商品查询
     */
    public List<Map<String, Object>> queryByProduct(Map<String, Object> queryParams) {
        // 商品关键词（必需）
        String productKeyword = ParamUtils.getQueryParam(queryParams, PRODUCT_KEYWORD, String.class);
        if (StrUtil.isBlank(productKeyword)) {
            log.warn("【按商品查询】商品关键词为空");
            return Collections.emptyList();
        }

        // 复制相关参数
        Map<String, Object> params = new HashMap<>();
        String[] paramKeys = {USER_ID, PRODUCT_KEYWORD, ORDER_BY, ORDER_DESC, LIMIT};
        ParamUtils.copyValidParams(queryParams, params, paramKeys);

        // 设置时间范围
        //setDefaultTimeRange(params, queryParams, DEFAULT_PRODUCT_QUERY_DAYS);

        // 设置分页
        params.put(LIMIT, queryParams.getOrDefault(LIMIT, DEFAULT_LIMIT));

        return convertOrders(orderMapper.selectByProductKeyword(params));
    }

    /**
     * 复合条件查询
     */
    public List<Map<String, Object>> queryByComplexConditions(Map<String, Object> queryParams) {
        Map<String, Object> params = new HashMap<>();

        // 复制相关参数
        String[] paramKeys = {USER_ID, ORDER_NUMBER, STATUS, PRODUCT_KEYWORD, START_TIME, END_TIME, ORDER_BY, ORDER_DESC, LIMIT};
        ParamUtils.copyValidParams(queryParams, params, paramKeys);

        // 设置时间范围
        //setDefaultTimeRange(params, params, DEFAULT_QUERY_DAYS);

        // 设置分页
        params.putIfAbsent(LIMIT, queryParams.getOrDefault(LIMIT, DEFAULT_LIMIT));

        return convertOrders(orderMapper.selectByCondition(params));
    }

    /**
     * 查询用户最近订单
     */
    public List<Map<String, Object>> queryUserRecentOrders(Map<String, Object> queryParams) {
        Long userId = ParamUtils.getQueryParam(queryParams, USER_ID, Long.class);
        if (userId == null) {
            log.warn("【按用户最近订单查询】用户ID为空");
            return Collections.emptyList();
        }
        int limit = (Integer) queryParams.getOrDefault(LIMIT, DEFAULT_USER_RECENT_LIMIT);
        return convertOrders(orderMapper.selectRecentOrdersByUserId(userId, limit));
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
        Long userId = ParamUtils.getQueryParam(queryParams, USER_ID, Long.class);
        if (userId == null) {
            log.warn("【智能查询】用户ID为空，无法查询");
            return Collections.emptyList();
        }

        // 3. 构建查询条件
        Map<String, Object> condition = buildIntelligentQueryCondition(queryParams);

        // 4. 执行查询
        return convertOrders(orderMapper.selectByCondition(condition));
    }

    /**
     * 构建智能查询条件
     */
    private Map<String, Object> buildIntelligentQueryCondition(Map<String, Object> queryParams) {
        Map<String, Object> condition = new HashMap<>();

        // 复制相关参数
        String[] paramKeys = {USER_ID, STATUS, PRODUCT_KEYWORD, START_TIME, END_TIME, ORDER_BY, ORDER_DESC, LIMIT};
        ParamUtils.copyValidParams(queryParams, condition, paramKeys);

        // 设置时间范围
        //setDefaultTimeRange(params, params, DEFAULT_QUERY_DAYS);

        // 设置默认分页
        condition.putIfAbsent(LIMIT, DEFAULT_LIMIT);

        log.debug("【智能查询】构建查询条件: {}", condition);
        return condition;
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
     * 检查订单访问权限
     */
    private boolean hasOrderAccessPermission(Order order, Map<String, Object> queryParams) {
        Long userId = ParamUtils.getQueryParam(queryParams, USER_ID, Long.class);
        if (userId != null && !userId.equals(order.getUserId())) {
            log.warn("【订单权限验证】订单不属于当前用户，订单用户ID: {}，当前用户ID: {}", order.getUserId(), userId);
            return false;
        }
        return true;
    }

    /**
     * 转换订单列表为Map列表
     */
    private List<Map<String, Object>> convertOrders(List<Order> orders) {
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());
    }

    /**
     * 设置默认时间范围
     */
    private void setDefaultTimeRange(Map<String, Object> targetParams, Map<String, Object> sourceParams, int defaultDays) {
        LocalDateTime now = LocalDateTime.now();

        // 处理开始时间
        if (sourceParams.containsKey(START_TIME) && sourceParams.get(START_TIME) != null) {
            targetParams.put(START_TIME, parseTimeField(sourceParams.get(START_TIME)));
        } else {
            targetParams.put(START_TIME, now.minusDays(defaultDays));
        }

        // 处理结束时间
        if (sourceParams.containsKey(END_TIME) && sourceParams.get(END_TIME) != null) {
            targetParams.put(END_TIME, parseTimeField(sourceParams.get(END_TIME)));
        } else {
            targetParams.put(END_TIME, now);
        }
    }

    /**
     * 解析时间字段（支持String和LocalDateTime）
     */
    private LocalDateTime parseTimeField(Object timeObj) {
        if (timeObj instanceof LocalDateTime) {
            return (LocalDateTime) timeObj;
        } else if (timeObj instanceof String) {
            try {
                return LocalDateTime.parse((String) timeObj, NORM_DATETIME_FORMATTER);
            } catch (Exception e) {
                log.warn("【订单查询】时间格式解析失败: {}", timeObj);
                return null;
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
