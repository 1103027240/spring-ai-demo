package cn.getech.base.demo.node.email;

import cn.getech.base.demo.dto.EmailClassifyDto;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 起草回复节点
 * @author 11030
 */
public class DraftResponseNode implements NodeAction {

    private ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        EmailClassifyDto emailClassifyDto = state.value("emailClassify")
                .map(v -> (EmailClassifyDto) v)
                .orElse(new EmailClassifyDto());

        String emailContent = state.value("email_content", "");

        List<String> contextSections = new ArrayList<>();
        List<String> searchResults = state.value("search_results", new ArrayList<>());
        if(CollUtil.isNotEmpty(searchResults)){
            String formattedDocs = searchResults.stream()
                    .map(doc -> "- " + doc)
                    .collect(Collectors.joining(" "));
            contextSections.add("相关文档: " + formattedDocs);
        }

        String draftPrompt = String.format("""
                为这封客户邮件起草回复：
                
                %s
                
                邮件意图：%s
                紧急程度：%s
                
                %s
                
                指南：
                - 专业且有帮助
                - 解决他们具体问题
                - 在相关时使用提供的文档
                
                """,
                emailContent,
                emailClassifyDto.getIntent(),
                emailClassifyDto.getUrgency(),
                StrUtil.join(" ", contextSections));

        // 调用AI大模型
        String response = qwenChatClient.prompt().user(draftPrompt).call().content();

        // 判断是否需要人工审核
        boolean needsReview = List.of("high", "critical").contains(emailClassifyDto.getUrgency())
                || "complex".equals(emailClassifyDto.getIntent());

        String nextNode = needsReview ? "human_review" : "send_reply";

        return Map.of(
                "draft_response", response,
                "next_node", nextNode
        );
    }

}
