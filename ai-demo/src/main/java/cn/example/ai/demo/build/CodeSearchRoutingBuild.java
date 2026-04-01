package cn.example.ai.demo.build;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CodeSearchRoutingBuild {

    @Resource(name = "qwenAgentChatModel")
    private Model qwenAgentChatModel;

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    public String extractText(Object mergedResult, String message, OverAllState state){
        if (mergedResult != null) {
            return multiAgentBuild.extractText(mergedResult);
        }

        List<String> resultTexts = Arrays.asList("github_result", "gitee_result", "csdn_result").stream()
                .map(e -> state.value(e, null))
                .filter(Objects::nonNull)
                .map(e -> multiAgentBuild.extractText(e))
                .collect(Collectors.toList());

        return synthesize(message, resultTexts);
    }

    public String synthesize(String query, List<String> agentResults) {
        if (CollUtil.isEmpty(agentResults)) {
            return "【RoutingMergeNode节点】返回结果为空";
        }

        try {
            // 1、封装请求参数
            List<Msg> messages = buildReqMessages(query, agentResults);

            // 2、调用大模型生成合并结果
            Flux<ChatResponse> responseFlux = qwenAgentChatModel.stream(messages, null, null);

            // 3、封装返回数据
            ChatResponse last = responseFlux.blockLast();
            if (last == null || CollUtil.isEmpty(last.getContent())) {
                return "【结果综合】\n由于模型调用返回结果为空，以下是原始结果的简单合并：\n" + String.join("\n---\n", agentResults);
            }

            return buildRespMessage(agentResults, last);
        } catch (Exception e) {
            return "【结果综合】\n由于模型调用失败，以下是原始结果的简单合并：\n" + String.join("\n---\n", agentResults);
        }
    }

    public List<Msg> buildReqMessages(String query, List<String> agentResults) {
        StringBuilder content = new StringBuilder();
        content.append("原始查询：").append(query).append("\n");
        content.append("以下是不同来源的搜索结果，请综合这些信息给出最佳回答：\n");

        for (int i = 0; i < agentResults.size(); i++) {
            content.append("来源").append(i + 1).append("：\n");
            content.append(agentResults.get(i)).append("\n");
        }

        return List.of(
                Msg.builder()
                        .role(MsgRole.SYSTEM)
                        .content(TextBlock.builder().text("你是一个结果合成专家，请将多个搜索结果整合成一个连贯、全面的回答。").build())
                        .build(),
                Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder().text(content.toString()).build())
                        .build());

    }

    public String buildRespMessage(List<String> agentResults, ChatResponse last){
        StringBuilder synthesized = new StringBuilder();
        synthesized.append("【模型综合结果】\n");
        synthesized.append("基于").append(agentResults.size()).append("个来源的结果综合如下：\n");

        String lastContent = last.getContent().stream()
                .filter(e -> !(e instanceof TextBlock))
                .map(e -> ((TextBlock) e).getText())
                .collect(Collectors.joining("\n"));
        synthesized.append(lastContent);

        return synthesized.toString();
    }

}
