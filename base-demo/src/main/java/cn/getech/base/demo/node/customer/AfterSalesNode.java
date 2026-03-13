package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.AfterSalesService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.getech.base.demo.enums.CustomerServiceNodeEnum.SENTIMENT_ANALYSIS;
import static cn.getech.base.demo.enums.SentimentAnalysisEnum.*;

/**
 * 售后处理节点
 * @author 11030
 */
@Slf4j
@Component
public class AfterSalesNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("【售后处理节点】开始执行");

        AfterSalesService afterSalesService = SpringUtil.getBean(AfterSalesService.class);
        String userInput = state.value(USER_INPUT, String.class).orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));
        Long userId = state.value(USER_ID, Long.class).orElse(null);
        String sentiment = state.value(SENTIMENT, String.class).orElse(NEUTRAL.getId());

        // 1.调用大模型分析售后类型
        Map<String, String> analysisAfterSalesMap = getAnalysisAfterSales(userInput);

        // 2.根据售后类型进行对应处理
        Map<String, Object> processResult = new HashMap<>();
        switch (analysisAfterSalesMap.get(AFTER_SALES_TYPE)) {
            case "return_request":
                processResult = afterSalesService.processReturnRequest(userInput, userId);
                break;
            case "exchange_request":
                processResult = afterSalesService.processExchangeRequest(userInput, userId);
                break;
            case "repair_request":
                processResult = afterSalesService.processRepairRequest(userInput, userId);
                break;
            case "refund_request":
                processResult = afterSalesService.processRefundRequest(userInput, userId);
                break;
            case "complaint":
                processResult = processComplaintRequest(userInput, sentiment, userId);
                break;
            case "progress_query":
                String serviceNumber = extractServiceNumber(userInput);
                if (StrUtil.isNotBlank(serviceNumber)) {
                    processResult = afterSalesService.queryAfterSalesProgress(serviceNumber);
                    break;
                }
                processResult.put(STATUS, "error");
                processResult.put(MESSAGE, "请提供服务单号以便查询进度");
                break;
            default:
                processResult.put(AFTER_SALES_TYPE, analysisAfterSalesMap.get(AFTER_SALES_TYPE));
                processResult.put(MESSAGE, "已记录您的售后请求，客服将尽快处理");
                break;
        }

        Map<String, Object> result = new HashMap<>();
        result.put(AFTER_SALES_TYPE, analysisAfterSalesMap.get(AFTER_SALES_TYPE));
        result.put(AFTER_SALES_ANALYSIS, analysisAfterSalesMap.get(AFTER_SALES_ANALYSIS));
        result.put(AFTER_SALES_RESULT, processResult);
        result.put(AFTER_SALES_TIME, System.currentTimeMillis());
        setPriority(result, sentiment);
        return result;
    }

    private Map<String, String> getAnalysisAfterSales(String userInput){
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        String analysisAfterSalesTypePrompt = """
                请分析用户的售后请求类型：
                1. return_request - 退货申请
                2. exchange_request - 换货申请
                3. repair_request - 维修申请
                4. refund_request - 退款申请
                5. complaint - 投诉
                6. progress_query - 进度查询
                7. other - 其他
                
                用户输入：%s
                
                请返回类型代码和简要分析，格式：类型代码|分析说明
                例如：return_request|申请退货
                """.formatted(userInput);

        // 调用大模型分析售后类型
        String analysisAfterSales = qwenChatClient.prompt()
                .user(analysisAfterSalesTypePrompt)
                .call()
                .content()
                .trim();

        String[] parts = analysisAfterSales.split("\\|", 2);
        String afterSalesType = parts[0].trim();
        String afterSalesAnalysis = parts.length > 1 ? parts[1].trim() : "";

        log.info("【售后处理节点】，售后类型分析: {} - {}", afterSalesType, afterSalesAnalysis);
        return Map.of(AFTER_SALES_TYPE, afterSalesType, AFTER_SALES_ANALYSIS, afterSalesAnalysis);
    }

    /**
     * 处理投诉
     */
    private Map<String, Object> processComplaintRequest(String userInput, String sentiment, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put(STATUS, "recorded");
        result.put(MESSAGE, "已收到您的投诉，我们将尽快处理");
        result.put(COMPLAINT_ID, "CP" + System.currentTimeMillis());
        result.put(USER_ID, userId);
        result.put(CONTENT, userInput);
        result.put(SENTIMENT, sentiment);
        result.put(CREATE_TIME, System.currentTimeMillis());
        setPriority(result, sentiment);
        return result;
    }

    /**
     * 设置优先级
     */
    private void setPriority(Map<String, Object> result, String sentiment){
        if (Arrays.asList(URGENT.getId(), NEGATIVE.getId()).contains(sentiment)) {
            result.put(PRIORITY, "high");
        } else {
            result.put(PRIORITY, "normal");
        }
    }

    /**
     * 提取服务单号
     */
    private String extractServiceNumber(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }

        Matcher matcher = SERVICE_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }

}
