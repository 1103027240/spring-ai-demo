package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.AfterSales;
import cn.getech.base.demo.entity.Order;
import cn.getech.base.demo.enums.AfterSalesStatusEnum;
import cn.getech.base.demo.enums.AfterSalesTypeEnum;
import cn.getech.base.demo.mapper.AfterSalesMapper;
import cn.getech.base.demo.service.AfterSalesService;
import cn.getech.base.demo.service.OrderService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import static cn.getech.base.demo.contant.FieldValueConstants.ORDER_NUMBER_PATTERN;
import static cn.getech.base.demo.enums.AfterSalesTypeEnum.*;
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

    /**
     * 处理退货申请
     */
    @Override
    public Map<String, Object> processReturnRequest(String userInput, Long userId) {
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
            result.put("message", String.format("退货申请未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        // 创建售后记录
        AfterSales afterSales = new AfterSales();
        String serviceNumber = generateServiceNumber("RS");
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(RETURNED.getCode()); // 退货申请
        afterSales.setReason("用户申请退货: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); // 待处理
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("message", "退货申请已提交，服务单号: " + serviceNumber + "，客服将在24小时内处理");
        result.put("serviceNumber", serviceNumber);
        result.put("nextSteps", Arrays.asList("1. 保持商品完好", "2. 等待客服联系", "3. 按指导寄回商品"));
        result.put("createTime", LocalDateTime.now());
        return result;
    }

    /**
     * 处理换货申请
     */
    @Override
    public Map<String, Object> processExchangeRequest(String userInput, Long userId) {
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
            result.put("message", String.format("退货申请未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        AfterSales afterSales = new AfterSales();
        String serviceNumber = generateServiceNumber("ES");
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(EXCHANGE.getCode()); // 换货申请
        afterSales.setReason("用户申请换货: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); // 待处理
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("message", "换货申请已提交，服务单号: " + serviceNumber + "，客服将尽快处理");
        result.put("serviceNumber", serviceNumber);
        result.put("nextSteps", Arrays.asList("1. 保持商品完好", "2. 等待客服联系", "3. 确认换货商品"));
        result.put("createTime", LocalDateTime.now());
        return result;
    }

    /**
     * 处理维修申请
     */
    @Override
    public Map<String, Object> processRepairRequest(String userInput, Long userId) {
        Map<String, Object> result = new HashMap<>();

        String orderNumber = extractOrderNumber(userInput);
        if (StrUtil.isBlank(orderNumber)) {
            result.put("status", "error");
            result.put("message", "未找到订单号，请提供订单号以便处理维修");
            return result;
        }

        Order order = orderService.getByOrderId(orderNumber);
        if (order == null) {
            result.put("status", "error");
            result.put("message", String.format("维修申请未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        AfterSales afterSales = new AfterSales();
        String serviceNumber = generateServiceNumber("RP");
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(REPAIR.getCode()); // 维修申请
        afterSales.setReason("用户申请维修: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); //待处理
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("message", "维修申请已提交，服务单号: " + serviceNumber + "，维修中心将尽快联系您");
        result.put("serviceNumber", serviceNumber);
        result.put("nextSteps", Arrays.asList("1. 描述具体故障现象", "2. 等待维修中心联系", "3. 确认维修方案"));
        result.put("createTime", LocalDateTime.now());
        return result;
    }

    /**
     * 处理退款申请
     */
    @Override
    public Map<String, Object> processRefundRequest(String userInput, Long userId) {
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
            result.put("message", String.format("退款申请未找到该订单[%s]，请检查订单号是否正确", orderNumber));
            return result;
        }

        AfterSales afterSales = new AfterSales();
        String serviceNumber = generateServiceNumber("RF");
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(order.getId());
        afterSales.setType(AfterSalesTypeEnum.REFUNDED.getCode()); // 退款申请
        afterSales.setReason("用户申请退款: " + userInput);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getCode()); // 待处理
        afterSales.setRefundAmount(BigDecimal.ZERO); // 需要根据订单计算
        afterSales.setCreateTime(LocalDateTime.now());
        afterSalesMapper.insert(afterSales);

        result.put("status", "success");
        result.put("message", "退款申请已提交，服务单号: " + serviceNumber);
        result.put("serviceNumber", serviceNumber);
        result.put("estimatedTime", "退款将在审核通过后3-5个工作日到账");
        result.put("createTime", LocalDateTime.now());
        return result;
    }

    /**
     * 处理查询售后进度
     */
    @Override
    public Map<String, Object> queryAfterSalesProgress(String serviceNumber) {
        Map<String, Object> result = new HashMap<>();

        AfterSales afterSales = afterSalesMapper.selectByServiceNumber(serviceNumber);
        if (afterSales == null) {
            result.put("status", "error");
            result.put("message", "售后进度查询，未找到该服务单号: " + serviceNumber);
            return result;
        }

        result.put("status", "success");
        result.put("serviceNumber", afterSales.getServiceNumber());
        result.put("typeCode", afterSales.getType());
        result.put("typeText", AfterSalesTypeEnum.getDetailDescription(afterSales.getType()));
        result.put("statusCode", afterSales.getStatus());
        result.put("statusText", AfterSalesStatusEnum.getDescription(afterSales.getStatus()));
        result.put("progressText", AfterSalesStatusEnum.getDetailDescription(afterSales.getStatus()));
        result.put("createTime", afterSales.getCreateTime());
        result.put("updateTime", afterSales.getUpdateTime());
        result.put("reason", afterSales.getReason());
        result.put("refundAmount", afterSales.getRefundAmount());
        result.put("solution", afterSales.getSolution());
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

    /**
     * 生成服务单号
     */
    private String generateServiceNumber(String prefix) {
        String dateStr = DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN);
        String randomNum = String.format("%08d", new Random().nextInt(99999999));
        return prefix + dateStr + randomNum;
    }

}
