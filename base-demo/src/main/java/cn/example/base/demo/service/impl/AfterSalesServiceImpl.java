package cn.example.base.demo.service.impl;

import cn.example.base.demo.entity.AfterSales;
import cn.example.base.demo.entity.Order;
import cn.example.base.demo.enums.AfterSalesStatusEnum;
import cn.example.base.demo.enums.AfterSalesTypeEnum;
import cn.example.base.demo.mapper.AfterSalesMapper;
import cn.example.base.demo.service.AfterSalesService;
import cn.example.base.demo.service.OrderService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.constant.FieldValueConstant.*;
import static cn.example.base.demo.constant.PatternConstant.ORDER_NUMBER_PATTERN;
import static cn.example.base.demo.enums.AfterSalesTypeEnum.*;
import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;

/**
 * @author 11030
 */
@Slf4j
@Service
public class AfterSalesServiceImpl extends ServiceImpl<AfterSalesMapper, AfterSales> implements AfterSalesService {

    @Autowired
    private OrderService orderService;

    private Random random = new Random();

    /**
     * 处理退货申请
     */
    @Override
    public Map<String, Object> processReturnRequest(String userInput, Long userId) {
        log.debug("【售后申请】开始处理退货申请，用户ID: {}, 输入: {}", userId, userInput);

        return processAfterSalesRequest(
                userInput,
                userId,
                RETURN_REQUEST,
                "RS",
                "退货",
                "客服将在24小时内处理",
                Arrays.asList("1. 保持商品完好", "2. 等待客服联系", "3. 按指导寄回商品")
        );
    }

    /**
     * 处理换货申请
     */
    @Override
    public Map<String, Object> processExchangeRequest(String userInput, Long userId) {
        log.debug("【售后申请】开始处理换货申请，用户ID: {}, 输入: {}", userId, userInput);

        return processAfterSalesRequest(
                userInput,
                userId,
                EXCHANGE_REQUEST,
                "ES",
                "换货",
                "客服将尽快处理",
                Arrays.asList("1. 保持商品完好", "2. 等待客服联系", "3. 确认换货商品")
        );
    }

    /**
     * 处理维修申请
     */
    @Override
    public Map<String, Object> processRepairRequest(String userInput, Long userId) {
        log.debug("【售后申请】开始处理维修申请，用户ID: {}, 输入: {}", userId, userInput);

        return processAfterSalesRequest(
                userInput,
                userId,
                REPAIR_REQUEST,
                "RP",
                "维修",
                "维修中心将尽快联系您",
                Arrays.asList("1. 描述具体故障现象", "2. 等待维修中心联系", "3. 确认维修方案")
        );
    }

    /**
     * 处理退款申请
     */
    @Override
    public Map<String, Object> processRefundRequest(String userInput, Long userId) {
        log.debug("【售后申请】开始处理退款申请，用户ID: {}, 输入: {}", userId, userInput);

        String orderNumber = extractOrderNumber(userInput);
        if (StrUtil.isBlank(orderNumber)) {
            return buildErrorResult(String.format(MESSAGE_ORDER_NOT_FOUND, "退款"));
        }

        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            return buildErrorResult(String.format(MESSAGE_ORDER_INVALID, orderNumber));
        }

        // 创建售后记录
        String serviceNumber = generateServiceNumber("RF");
        AfterSales afterSales = createAfterSalesRecord(order.getId(), userId, REFUND_REQUEST.getId(), serviceNumber, "用户申请退款: " + userInput);
        afterSales.setRefundAmount(BigDecimal.ZERO); // 需要根据订单计算
        baseMapper.insert(afterSales);

        Map<String, Object> result = buildSuccessResult(String.format(MESSAGE_APPLICATION_SUBMITTED, "退款", serviceNumber, "退款将在审核通过后3-5个工作日到账"), serviceNumber);
        result.put(ESTIMATED_TIME, "退款将在审核通过后3-5个工作日到账");
        result.put(CREATE_TIME, LocalDateTime.now());
        return result;
    }

    /**
     * 处理查询售后进度
     */
    @Override
    public Map<String, Object> queryAfterSalesProgress(String serviceNumber) {
        log.debug("【售后查询】开始查询售后进度，服务单号: {}", serviceNumber);
        if (StrUtil.isBlank(serviceNumber)) {
            return buildErrorResult("服务单号不能为空");
        }

        AfterSales afterSales = baseMapper.selectByServiceNumber(serviceNumber);
        if (afterSales == null) {
            log.warn("【售后查询】服务单号不存在: {}", serviceNumber);
            return buildErrorResult("售后进度查询，未找到该服务单号: " + serviceNumber);
        }

        Map<String, Object> result = new HashMap<>();
        result.put(STATUS, STATUS_SUCCESS);
        result.put(SERVICE_NUMBER, afterSales.getServiceNumber());
        result.put(TYPE, afterSales.getType());
        result.put(TYPE_TEXT, AfterSalesTypeEnum.getDetailText(afterSales.getType()));
        result.put(STATUS, afterSales.getStatus());
        result.put(STATUS_TEXT, AfterSalesStatusEnum.getText(afterSales.getStatus()));
        result.put(PROGRESS_TEXT, AfterSalesStatusEnum.getDetailText(afterSales.getStatus()));
        result.put(CREATE_TIME, afterSales.getCreateTime());
        result.put(UPDATE_TIME, afterSales.getUpdateTime());
        result.put(REASON, afterSales.getReason());
        result.put(REFUND_AMOUNT, afterSales.getRefundAmount());
        result.put(SOLUTION, afterSales.getSolution());

        log.debug("【售后查询】售后进度查询成功，服务单号: {}, 状态: {}", serviceNumber, afterSales.getStatus());
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
        String randomNum = String.format("%0" + RANDOM_NUMBER_FORMAT_LENGTH + "d", random.nextInt(RANDOM_NUMBER_BOUND));
        return prefix + dateStr + randomNum;
    }

    /**
     * 处理售后申请
     */
    private Map<String, Object> processAfterSalesRequest(String userInput, Long userId, AfterSalesTypeEnum type, String prefix,
                                                         String typeName, String processTime, List<String> nextSteps) {
        String orderNumber = extractOrderNumber(userInput);
        if (StrUtil.isBlank(orderNumber)) {
            return buildErrorResult(String.format(MESSAGE_ORDER_NOT_FOUND, typeName));
        }

        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            return buildErrorResult(String.format(MESSAGE_ORDER_INVALID, orderNumber));
        }

        // 创建售后记录
        String serviceNumber = generateServiceNumber(prefix);
        AfterSales afterSales = createAfterSalesRecord(order.getId(), userId, type.getId(), serviceNumber, String.format("用户申请%s: %s", typeName, userInput));
        baseMapper.insert(afterSales);
        log.info("【售后申请】{}申请提交成功，用户ID: {}, 订单号: {}, 服务单号: {}", typeName, userId, orderNumber, serviceNumber);

        Map<String, Object> result = buildSuccessResult(String.format(MESSAGE_APPLICATION_SUBMITTED, typeName, serviceNumber, processTime), serviceNumber);
        result.put(NEXT_STEPS, nextSteps);
        result.put(CREATE_TIME, LocalDateTime.now());
        return result;
    }

    /**
     * 创建售后记录
     */
    private AfterSales createAfterSalesRecord(Long orderId, Long userId, Integer typeCode, String serviceNumber, String reason) {
        AfterSales afterSales = new AfterSales();
        afterSales.setServiceNumber(serviceNumber);
        afterSales.setOrderId(orderId);
        afterSales.setType(typeCode);
        afterSales.setReason(reason);
        afterSales.setStatus(AfterSalesStatusEnum.PENDING.getId());
        afterSales.setCreateTime(LocalDateTime.now());
        afterSales.setUserId(userId);
        return afterSales;
    }

    /**
     * 构建成功结果
     */
    private Map<String, Object> buildSuccessResult(String message, String serviceNumber) {
        Map<String, Object> result = new HashMap<>();
        result.put(STATUS, STATUS_SUCCESS);
        result.put(MESSAGE, message);
        result.put(SERVICE_NUMBER, serviceNumber);
        return result;
    }

    /**
     * 构建错误结果
     */
    private Map<String, Object> buildErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put(STATUS, STATUS_ERROR);
        result.put(MESSAGE, message);
        log.warn("【售后服务】错误: {}", message);
        return result;
    }

}
