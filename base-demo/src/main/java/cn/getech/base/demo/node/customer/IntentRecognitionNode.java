package cn.getech.base.demo.node.customer;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * 意图识别节点
 * @author 11030
 */
@Slf4j
@Component
public class IntentRecognitionNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("开始意图识别节点");
        String userInput = state.value("userInput", String.class)
                .orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

        // 构建意图识别提示词
        String intentRecognitionPrompt = """
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
                """.formatted(userInput);

        // 调用大模型进行意图识别
        String intentRecognition = qwenChatClient.prompt()
                .user(intentRecognitionPrompt)
                .call()
                .content()
                .trim();
        log.info("意图识别结果: {}", intentRecognition);

        // 将意图结果存入状态
        Map<String, Object> result = new HashMap<>();
        result.put("intentRecognition", intentRecognition);
        result.put("intentRecognitionTime", System.currentTimeMillis());

        return result;
    }

}
