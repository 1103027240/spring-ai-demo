package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.AfterSales;
import cn.getech.base.demo.entity.Order;
import cn.getech.base.demo.enums.AfterSalesStatusEnum;
import cn.getech.base.demo.enums.AfterSalesTypeEnum;
import cn.getech.base.demo.mapper.AfterSalesMapper;
import cn.getech.base.demo.service.AfterSalesService;
import cn.getech.base.demo.service.OrderService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;

/**
 * @author 11030
 */
@Slf4j
@Service
public class AfterSalesServiceImpl implements AfterSalesService {

    @Autowired
    private AfterSalesMapper afterSalesMapper;

    @Autowired
    private OrderService orderService;

    // 订单号正则表达式
    private final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("([A-Z]{3,6}\\d{6,12})|(\\d{6,12})");

    /**
     * 处理退货申请
     */
    @Override
    public Map<String, Object> processReturnRequest(String userInput) {
        log.info("处理退货申请: {}", userInput);
        Map<String, Object> result = new HashMap<>();

        // 提取订单号
        String orderNumber = extractOrderNumber(userInput);
        if (StrUtil.isBlank(orderNumber)) {
            result.put("status", "error");
            result.put("message", "未找到订单号，请提供订单号以便处理退货");
            return result;
        }

        Order order = orderService.getByOrderId(orderNumber);
        if (order == null) {
            result.put("status", "error");
            result.put("message", String.format("未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        // 生成服务单号
        String serviceNumber = "RS" + DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN) + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建售后记录
        AfterSales afterSales = new AfterSales();
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(AfterSalesTypeEnum.REFUNDED.getCode()); // 退货
        afterSales.setReason("用户申请退货: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); // 待处理
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("serviceNumber", serviceNumber);
        result.put("message", "退货申请已提交，服务单号: " + serviceNumber + "，客服将在24小时内处理");
        result.put("nextSteps", "1. 保持商品完好 2. 等待客服联系 3. 按指导寄回商品");

        return result;
    }

    /**
     * 处理换货申请
     */
    @Override
    public Map<String, Object> processExchangeRequest(String userInput) {
        log.info("处理换货申请: {}", userInput);
        Map<String, Object> result = new HashMap<>();

        String orderNumber = extractOrderNumber(userInput);
        if (StrUtil.isBlank(orderNumber)) {
            result.put("status", "error");
            result.put("message", "未找到订单号，请提供订单号以便处理换货");
            return result;
        }

        Order order = orderService.getByOrderId(orderNumber);
        if (order == null) {
            result.put("status", "error");
            result.put("message", String.format("未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        String serviceNumber = "ES" + DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN) + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        AfterSales afterSales = new AfterSales();
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(AfterSalesTypeEnum.EXCHANGE.getCode()); // 换货
        afterSales.setReason("用户申请换货: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); // 待处理
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("serviceNumber", serviceNumber);
        result.put("message", "换货申请已提交，服务单号: " + serviceNumber + "，客服将尽快处理");
        result.put("nextSteps", "1. 保持商品完好 2. 等待客服联系 3. 确认换货商品");

        return result;
    }

    /**
     * 处理退款申请
     */
    @Override
    public Map<String, Object> processRefundRequest(String userInput) {
        log.info("处理退款申请: {}", userInput);
        Map<String, Object> result = new HashMap<>();

        String orderNumber = extractOrderNumber(userInput);
        if (StrUtil.isBlank(orderNumber)) {
            result.put("status", "error");
            result.put("message", "未找到订单号，请提供订单号以便处理退款");
            return result;
        }

        Order order = orderService.getByOrderId(orderNumber);
        if (order == null) {
            result.put("status", "error");
            result.put("message", String.format("未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        String serviceNumber = "RF" + DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN) + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        AfterSales afterSales = new AfterSales();
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(AfterSalesTypeEnum.REFUNDED.getCode()); // 退货退款
        afterSales.setReason("用户申请退款: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); // 待处理
        afterSales.setRefundAmount(BigDecimal.ZERO); // 需要根据订单计算
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("serviceNumber", serviceNumber);
        result.put("message", "退款申请已提交，服务单号: " + serviceNumber);
        result.put("estimatedTime", "退款将在审核通过后3-5个工作日到账");

        return result;
    }

    /**
     * 查询售后进度
     */
    @Override
    public Map<String, Object> queryAfterSalesProgress(String serviceNumber) {
        AfterSales afterSales = afterSalesMapper.selectByServiceNumber(serviceNumber);
        if (afterSales == null) {
            throw new RuntimeException("未找到服务单号: " + serviceNumber);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("serviceNumber", afterSales.getServiceNumber());
        result.put("type", AfterSalesTypeEnum.getDescription(afterSales.getType()));
        result.put("status", AfterSalesStatusEnum.getDescription(afterSales.getStatus()));
        result.put("reason", afterSales.getReason());
        result.put("createTime", afterSales.getCreateTime());
        result.put("updateTime", afterSales.getUpdateTime());

        if (afterSales.getRefundAmount() != null) {
            result.put("refundAmount", afterSales.getRefundAmount());
        }

        if (StrUtil.isNotBlank(afterSales.getSolution())) {
            result.put("solution", afterSales.getSolution());
        }

        return result;
    }

    /**
     * 提取订单号
     */
    private String extractOrderNumber(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }

        Matcher matcher = ORDER_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

}
