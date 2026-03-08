package cn.getech.base.demo.node.email;

import cn.getech.base.demo.dto.EmailClassifyDto;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分类意图节点
 * @author 11030
 */
public class ClassifyIntentNode implements NodeAction {

    private ChatClient qwenChatClient = SpringUtil.getBean("qwenChatClient");

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 读取邮件内容和发送人（生产环境连接邮件服务）
        String emailContent = state.value("email_content")
                .map(e -> (String) e)
                .orElseThrow(() -> new RuntimeException("no email content"));

        String senderEmail = state.value("sender_email", "unknown");

        String classifyPrompt = String.format("""
                    分析这封客户邮件并进行分类
                    
                    邮件：%s
                    发送人：%s
                    
                    提供分类，包括意图、紧急程度、主题、摘要
                    意图应该是以下之一：question、bug、billing、feature、complex
                    紧急程度应是以下之一：low、medium、high、critical
                    
                    以JSON形式返回，{"intent": "...", "urgency": "...", "topic": "...", "summary": "..."}
                """, emailContent, senderEmail);

        // 调用AI大模型
        String response = qwenChatClient.prompt().user(classifyPrompt).call().content();

        EmailClassifyDto emailClassifyDto = parseEmailClassify(response);
        String nextNode;
        if ("billing".equals(emailClassifyDto.getIntent()) || "critical".equals(emailClassifyDto.getUrgency())) {
            nextNode = "human_review";
        } else if (List.of("question", "feature").contains(emailClassifyDto.getIntent())) {
            nextNode = "search_document";
        } else if ("bug".equals(emailClassifyDto.getIntent())) {
            nextNode = "bug_track";
        } else {
            nextNode = "draft_response";
        }

        return Map.of("emailClassify", emailClassifyDto, "nextNode", nextNode);
    }

    private EmailClassifyDto parseEmailClassify(String jsonResponse) {
        EmailClassifyDto classifyDto = new EmailClassifyDto();

        // 简单的正则表达式解析
        Pattern intentPattern = Pattern.compile("\"intent\"\\s*:\\s*\"([^\"]+)\"");
        Pattern urgencyPattern = Pattern.compile("\"urgency\"\\s*:\\s*\"([^\"]+)\"");
        Pattern topicPattern = Pattern.compile("\"topic\"\\s*:\\s*\"([^\"]+)\"");
        Pattern summaryPattern = Pattern.compile("\"summary\"\\s*:\\s*\"([^\"]+)\"");

        Matcher matcher = intentPattern.matcher(jsonResponse);
        if (matcher.find()) {
            classifyDto.setIntent(matcher.group(1));
        }

        matcher = urgencyPattern.matcher(jsonResponse);
        if (matcher.find()) {
            classifyDto.setUrgency(matcher.group(1));
        }

        matcher = topicPattern.matcher(jsonResponse);
        if (matcher.find()) {
            classifyDto.setTopic(matcher.group(1));
        }

        matcher = summaryPattern.matcher(jsonResponse);
        if (matcher.find()) {
            classifyDto.setSummary(matcher.group(1));
        }

        // 如果解析失败，设置默认值
        if (classifyDto.getIntent() == null) {
            classifyDto.setIntent("question");
        }
        if (classifyDto.getUrgency() == null) {
            classifyDto.setUrgency("medium");
        }
        if (classifyDto.getTopic() == null) {
            classifyDto.setTopic("general");
        }
        if (classifyDto.getSummary() == null) {
            classifyDto.setSummary("需要处理的客户邮件");
        }

        return classifyDto;
    }

}
