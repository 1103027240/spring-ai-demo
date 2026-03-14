package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.enums.IntentRecognitionEnum;
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
import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.enums.IntentRecognitionEnum.*;

/**
 * 意图识别节点
 * @author 11030
 */
@Slf4j
@Component
public class IntentRecognitionNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("【意图识别节点】开始执行");

        String userInput = state.value(USER_INPUT, String.class).orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        // 调用大模型进行意图识别
        String intent = getIntentRecognition(userInput);

        Map<String, Object> result = new HashMap<>();
        result.put(INTENT, intent);
        result.put(INTENT_RECOGNITION_TIME, System.currentTimeMillis());
        return result;
    }

    private String getIntentRecognition(String userInput){
        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        String intentPrompt = """
                请分析用户的咨询意图，从以下类别中选择最匹配的一项：
                1. order_query - 订单查询（如：我的订单状态、物流信息）
                2. product_info - 商品信息（如：产品规格、价格、库存）
                3. after_sales - 售后服务（如：退货、换货、维修）
                4. payment_issue - 支付问题（如：支付失败、退款）
                5. logistics_query - 物流查询（如：快递状态、配送时间）
                6. policy_question - 政策咨询（如：退货政策、保修政策）
                7. complaint - 投诉建议
                8. general_question - 一般咨询
                
                用户输入：%s
                
                请只返回意图类别代码，不要返回其他内容。
                例如：如果用户问"我的订单到哪里了"，返回order_query
                """.formatted(userInput);

        // 调用大模型进行意图识别
        String intent = qwenChatClient.prompt()
                .user(intentPrompt)
                .call()
                .content()
                .trim();

        intent = validateAndNormalizeIntentRecognition(intent);
        log.info("【意图识别节点】识别结果: {}", intent);
        return intent;
    }

    /**
     * 验证和规范化意图
     */
    private String validateAndNormalizeIntentRecognition(String intent) {
        if (StrUtil.isBlank(intent)) {
            return GENERAL_QUESTION.getId();
        }

        String normalized = intent.trim().toLowerCase();

        // 检查是否是合法的意图
        String validIntent = Arrays.asList(IntentRecognitionEnum.values()).stream()
                .filter(e -> normalized.contains(e.getId()))
                .findFirst()
                .map(IntentRecognitionEnum::getId)
                .orElse("");
        if(StrUtil.isNotBlank(validIntent)){
            return validIntent;
        }

        // 如果包含特定关键词，进行映射
        if (normalized.contains("order") || normalized.contains("订单")) {
            return ORDER_QUERY.getId();
        } else if (normalized.contains("product") || normalized.contains("商品") || normalized.contains("产品")) {
            return IntentRecognitionEnum.PRODUCT_INFO.getId();
        } else if (normalized.contains("after_sales") || normalized.contains("售后") || normalized.contains("退货") || normalized.contains("换货") || normalized.contains("退款")) {
            return AFTER_SALES.getId();
        } else if (normalized.contains("payment") || normalized.contains("支付") || normalized.contains("付款")) {
            return PAYMENT_ISSUE.getId();
        } else if (normalized.contains("logistics") || normalized.contains("物流") || normalized.contains("快递") || normalized.contains("发货")) {
            return LOGISTICS_QUERY.getId();
        } else if (normalized.contains("policy") || normalized.contains("政策") || normalized.contains("规则")) {
            return POLICY_QUESTION.getId();
        } else if (normalized.contains("complaint") || normalized.contains("投诉") || normalized.contains("抱怨")) {
            return COMPLAINT.getId();
        } else {
            return GENERAL_QUESTION.getId();
        }
    }

}
