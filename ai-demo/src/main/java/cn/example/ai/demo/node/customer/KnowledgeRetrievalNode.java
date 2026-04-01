package cn.example.ai.demo.node.customer;

import cn.example.ai.demo.enums.IntentRecognitionEnum;
import cn.example.ai.demo.service.KnowledgeDocumentService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static cn.example.ai.demo.constant.FieldConstant.*;
import static cn.example.ai.demo.enums.IntentRecognitionEnum.GENERAL_QUESTION;

/**
 * 知识库检索节点
 * @author 11030
 */
@Slf4j
@Component
public class KnowledgeRetrievalNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("【知识库检索节点】开始执行");

        KnowledgeDocumentService knowledgeDocumentService = SpringUtil.getBean(KnowledgeDocumentService.class);
        String userInput = state.value(USER_INPUT, String.class).orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));
        String intent = state.value(INTENT, String.class).orElse(GENERAL_QUESTION.getId());
        String query = buildKnowledgeQuery(userInput, intent);

        // 从知识库检索相关信息
        List<Map<String, Object>> knowledgeResults = knowledgeDocumentService.searchKnowledge(query, 100);
        log.info("【知识库检索节点】检索完成，找到[{}]条结果", knowledgeResults.size());

        StringBuilder knowledgeContext = new StringBuilder();
        if (CollUtil.isEmpty(knowledgeResults)) {
            knowledgeContext.append(getDefaultKnowledge(intent));
        } else {
            AtomicInteger i = new AtomicInteger(1);
            knowledgeResults.forEach(e -> {
                knowledgeContext.append(String.format("【相关知识点%d】\n", i.getAndIncrement()));
                knowledgeContext.append(String.format("标题：%s\n", e.get(TITLE)));
                knowledgeContext.append(String.format("内容：%s\n", e.get(CONTENT)));
            });
        }

        Map<String, Object> result = new HashMap<>();
        result.put(KNOWLEDGE_RESULTS, knowledgeResults);
        result.put(KNOWLEDGE_CONTEXT, knowledgeContext.toString());
        result.put(KNOWLEDGE_RETRIEVAL_TIME, System.currentTimeMillis());
        return result;
    }

    /**
     * 构建知识库查询
     */
    private String buildKnowledgeQuery(String userInput, String intent) {
        // 基于意图优化查询
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(userInput);

        IntentRecognitionEnum intentRecognitionEnum = IntentRecognitionEnum.valueOf(intent.toUpperCase(Locale.ROOT));
        switch (intentRecognitionEnum) {
            case ORDER_QUERY:
                queryBuilder.append(" 订单查询 物流状态 发货时间");
                break;
            case PRODUCT_INFO:
                queryBuilder.append(" 商品信息 产品规格 价格 库存");
                break;
            case AFTER_SALES:
                queryBuilder.append(" 售后 退货 换货 退款 维修");
                break;
            case PAYMENT_ISSUE:
                queryBuilder.append(" 支付 付款 退款 失败");
                break;
            case LOGISTICS_QUERY:
                queryBuilder.append(" 物流 快递 配送 发货");
                break;
            case POLICY_QUESTION:
                queryBuilder.append(" 政策 规则 条款 说明");
                break;
            case COMPLAINT:
                queryBuilder.append(" 投诉 建议 反馈");
                break;
            default:
                break;
        }
        return queryBuilder.toString();
    }

    /**
     * 获取默认知识
     */
    private String getDefaultKnowledge(String intent) {
        IntentRecognitionEnum intentRecognitionEnum = IntentRecognitionEnum.valueOf(intent.toUpperCase(Locale.ROOT));
        switch (intentRecognitionEnum) {
            case ORDER_QUERY:
                return "【订单查询帮助】\n您可以提供订单号，或者登录账户查看订单详情。\n";
            case AFTER_SALES:
                return "【售后服务】\n支持7天无理由退货，质量问题免费退换。\n";
            case LOGISTICS_QUERY:
                return "【物流查询】\n工作日16:00前下单当天发货，物流时效3-5天。\n";
            case PAYMENT_ISSUE:
                return "【支付问题】\n支持支付宝、微信支付、银行卡支付。\n";
            default:
                return "【客服帮助】\n您好，请问有什么可以帮您？\n";
        }
    }

}
