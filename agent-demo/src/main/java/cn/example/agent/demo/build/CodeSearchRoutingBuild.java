package cn.example.agent.demo.build;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static cn.example.agent.demo.constant.FieldConstant.OUT_RESULT;

@Component
public class CodeSearchRoutingBuild {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    public String extractText(Object mergedResult, String message, OverAllState state){
        if (mergedResult != null) {
            return multiAgentBuild.extractText(mergedResult);
        }

        List<String> resultTexts = Arrays.asList(OUT_RESULT).stream()
                .map(e -> state.value(e, null))
                .filter(Objects::nonNull)
                .map(e -> multiAgentBuild.extractText(e))
                .collect(Collectors.toList());

        return synthesize(message, resultTexts);
    }

    public String synthesize(String query, List<String> agentResults) {
        if (CollUtil.isEmpty(agentResults)) {
            return "【LlmRoutingNode节点】返回结果为空";
        }

        try {
            // 1、封装请求参数
            List<Message> messages = buildReqMessages(query, agentResults);

            // 2、调用大模型生成合并结果
            ReactAgent agent = ReactAgent.builder()
                    .name("合成智能体")
                    .model(qwenChatModel)
                    .build();
            AssistantMessage response = agent.call(messages);

            // 3、封装返回数据
            if (StrUtil.isBlank(response.getText())) {
                return "【结果综合】\n由于模型调用返回结果为空，以下是原始结果的简单合并：\n" + String.join("\n---\n", agentResults);
            }

            return buildRespMessage(agentResults, response.getText());
        } catch (Exception e) {
            return "【结果综合】\n由于模型调用失败，以下是原始结果的简单合并：\n" + String.join("\n---\n", agentResults);
        }
    }

    public List<Message> buildReqMessages(String query, List<String> agentResults) {
        StringBuilder content = new StringBuilder();
        content.append("原始查询：").append(query).append("\n");
        content.append("以下是不同来源的搜索结果，请综合这些信息给出最佳回答：\n");

        for (int i = 0; i < agentResults.size(); i++) {
            content.append("来源").append(i + 1).append("：\n");
            content.append(agentResults.get(i)).append("\n");
        }

        return List.of(
                SystemMessage.builder()
                        .text("你是一个结果合成专家，请将多个搜索结果整合成一个连贯、全面的回答。")
                        .build(),
                UserMessage.builder()
                        .text(content.toString())
                        .build());

    }

    public String buildRespMessage(List<String> agentResults, String context){
        StringBuilder synthesized = new StringBuilder();
        synthesized.append("【模型综合结果】\n");
        synthesized.append("基于").append(agentResults.size()).append("个来源的结果综合如下：\n");
        synthesized.append(context);
        return synthesized.toString();
    }

}
