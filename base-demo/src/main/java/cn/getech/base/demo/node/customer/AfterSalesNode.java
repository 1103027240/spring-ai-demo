package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.AfterSalesService;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * 售后处理节点
 * @author 11030
 */
@Slf4j
@Component
public class AfterSalesNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("开始售后处理节点");
        String userInput = state.value("userInput", String.class)
                .orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");
        AfterSalesService afterSalesService = SpringUtil.getBean("afterSalesService");

        // 分析售后类型
        String analysisPrompt = """
                请分析用户的售后请求类型：
                1. return_request - 退货申请
                2. exchange_request - 换货申请
                3. repair_request - 维修申请
                4. refund_request - 退款申请
                5. complaint - 投诉
                6. other - 其他
                
                用户输入：%s
                
                请返回类型代码和简要分析，格式：类型代码|分析说明
                """.formatted(userInput);

        String analysisResult = qwenChatClient.prompt()
                .user(analysisPrompt)
                .call()
                .content()
                .trim();

        String[] parts = analysisResult.split("\\|", 2);
        String afterSalesType = parts[0].trim();
        String analysis = parts.length > 1 ? parts[1].trim() : "";
        log.info("售后类型分析: {} - {}", afterSalesType, analysis);

        // 根据类型处理
        Map<String, Object> processResult = new HashMap<>();
        switch (afterSalesType) {
            case "return_request":
                processResult = afterSalesService.processReturnRequest(userInput);
                break;
            case "exchange_request":
                processResult = afterSalesService.processExchangeRequest(userInput);
                break;
            case "refund_request":
                processResult = afterSalesService.processRefundRequest(userInput);
                break;
            default:
                processResult.put("type", afterSalesType);
                processResult.put("message", "已记录您的售后请求，客服将尽快处理");
                break;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("afterSalesType", afterSalesType);
        result.put("afterSalesAnalysis", analysis);
        result.put("afterSalesResult", processResult);
        result.put("afterSalesTime", System.currentTimeMillis());

        return result;
    }

}
