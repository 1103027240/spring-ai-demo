package cn.getech.base.demo.node.customer;

import cn.getech.base.demo.service.KnowledgeBaseService;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库检索节点
 * @author 11030
 */
@Slf4j
@Component
public class KnowledgeRetrievalNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        log.info("开始知识库检索节点");
        String userInput = state.value("userInput", String.class)
                .orElseThrow(() -> new IllegalArgumentException("用户输入不能为空"));

        KnowledgeBaseService knowledgeBaseService = SpringUtil.getBean("knowledgeBaseService");

        // 从知识库检索相关信息
        List<Map<String, Object>> knowledgeResults = knowledgeBaseService.searchKnowledge(userInput, 3);
        log.info("知识库检索到[{}]条相关结果", knowledgeResults.size());

        StringBuilder knowledgeContext = new StringBuilder();
        for (int i = 0; i < knowledgeResults.size(); i++) {
            Map<String, Object> result = knowledgeResults.get(i);
            knowledgeContext.append(String.format("【知识%d】标题：%s\n内容：%s\n\n",
                    i + 1,
                    result.get("title"),
                    result.get("content")));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("knowledgeResults", knowledgeResults);
        result.put("knowledgeContext", knowledgeContext.toString());
        result.put("knowledgeRetrievalTime", System.currentTimeMillis());

        return result;
    }

}
