package cn.example.ai.demo.memory;

import cn.example.ai.demo.converter.DocumentConverter;
import cn.example.ai.demo.entity.LongTermChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分层记忆：短期Redis + 长期Milvus向量库
 * @author 11030
 */
public class HierarchicalChatMemory implements ChatMemory {

    // 短期记忆（Redis存储）
    private ChatMemory shortTermMemory;

    // 长期记忆（Milvus向量库存储）
    private VectorStore longTermMemoryVectorStore;

    // 短期记忆最大消息数
    @Value("${spring.ai.memory.short-term.max-messages:10}")
    private int shortTermMaxMessages;

    // 长期记忆搜索前K条消息
    @Value("${spring.ai.memory.long-term.similarity-top-k:6}")
    private int longTermTopK;

    public HierarchicalChatMemory(ChatMemory shortTermMemory, VectorStore longTermMemoryVectorStore) {
        this.shortTermMemory = shortTermMemory;
        this.longTermMemoryVectorStore = longTermMemoryVectorStore;
    }

    /**
     * 添加消息：先写短期记忆，溢出则摘要后写入长期记忆
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 1. 写入短期记忆
        shortTermMemory.add(conversationId, messages);

        // 2. 获取当前短期记忆所有消息
        List<Message> currentShortTermMessages = shortTermMemory.get(conversationId);

        // 3. 检查短期记忆是否溢出，溢出则摘要后存入长期记忆
        if (currentShortTermMessages.size() > shortTermMaxMessages) {
            moveToLongTermMemory(conversationId, currentShortTermMessages, shortTermMemory);
        }
    }

    /**
     * 获取记忆：合并短期记忆 + 长期记忆
     */
    @Override
    public List<Message> get(String conversationId) {
        // 1. 获取短期记忆
        List<Message> shortTermMessages = shortTermMemory.get(conversationId);
        if (shortTermMessages.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 从Milvus召回当前会话相关长期记忆
        List<Message> longTermMessages = recallLongTermMemory(conversationId, shortTermMessages);

        // 3. 合并记忆：长期记忆 + 短期记忆
        List<Message> mergedMessages = new ArrayList<>();
        mergedMessages.addAll(longTermMessages);
        mergedMessages.addAll(shortTermMessages);

        return mergedMessages;
    }

    /**
     * 清空短期记忆（仅清空短期，长期记忆保留）
     */
    @Override
    public void clear(String conversationId) {
        // 清空短期记忆
        shortTermMemory.clear(conversationId);

        // 可选：清空当前会话的长期记忆（根据业务需求）
//        longTermMemoryStore.delete(longTermMemoryStore.similaritySearch(
//                SearchRequest.builder()
//                        .query("")
//                        .filterExpression("conversationId == '" + conversationId + "'")
//                        .build())
//                .stream()
//                .map(Document::getId)
//                .collect(Collectors.toList()));
    }

    /**
     * 短期记忆溢出时，摘要后写入长期Milvus
     */
    private void moveToLongTermMemory(String conversationId, List<Message> allShortTermMessages, ChatMemory shortTermMemory) {
        // 1. 拆分：保留最新N条，其余转入长期
        List<Message> keepMessages = allShortTermMessages.subList(
                allShortTermMessages.size() - shortTermMaxMessages,
                allShortTermMessages.size());

        List<Message> toLongTermMessages = allShortTermMessages.subList(
                0,
                allShortTermMessages.size() - shortTermMaxMessages);

        // 2. 生成记忆摘要（简化版：拼接消息，生产可调用LLM生成精简摘要）
        String memorySummary = generateMemorySummary(conversationId, toLongTermMessages);

        // 3. 写入Milvus向量库：id由Milvus自动生成，memoryVector由EmbeddingModel通过memory自动向量化生成
        LongTermChatMemory longTermChatMemory = LongTermChatMemory.builder()
                .content(memorySummary)
                .conversationId(conversationId)
                .createTime(String.valueOf(System.currentTimeMillis()))
                .memoryType("LONG_TERM")
                .build();

        Document doc = DocumentConverter.toDocument(
                longTermChatMemory,
                LongTermChatMemory::getContent,
                e -> Map.of(
                        "conversationId", longTermChatMemory.getConversationId(),
                        "createTime", longTermChatMemory.getCreateTime(),
                        "memoryType", longTermChatMemory.getMemoryType()));
        longTermMemoryVectorStore.add(List.of(doc));

        // 4. 重置短期记忆，只保留最新N条
        shortTermMemory.clear(conversationId);
        shortTermMemory.add(conversationId, keepMessages);
    }

    /**
     * 生成记忆摘要
     */
    private String generateMemorySummary(String conversationId, List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("会话ID：").append(conversationId).append("\n").append("历史对话摘要：\n");
        String conversationContent = messages.stream()
                .map(msg -> msg.getMessageType().name() + ": " + msg.getText())
                .collect(Collectors.joining("\n"));
        sb.append(conversationContent);
        return sb.toString();

    }

    /**
     * 从Milvus召回相关长期记忆
     */
    private List<Message> recallLongTermMemory(String conversationId, List<Message> shortTermMessages) {
        // 1.提取最新用户问题作为检索关键词
        String latestQuery = shortTermMessages.stream()
                .filter(msg -> MessageType.USER.equals(msg.getMessageType()))
                .reduce((first, second) -> second)
                .map(Message::getText)
                .orElse("");

        // 2. 从Milvus召回相关长期记忆
        SearchRequest searchRequest = SearchRequest.builder()
                .query(latestQuery)
                .topK(longTermTopK)
                .filterExpression("conversationId == '" + conversationId + "'")
                .build();

        return longTermMemoryVectorStore.similaritySearch(searchRequest).stream()
                .map(doc -> new UserMessage("【历史相关记忆】：" + doc.getText()))
                .collect(Collectors.toList());
    }

}
