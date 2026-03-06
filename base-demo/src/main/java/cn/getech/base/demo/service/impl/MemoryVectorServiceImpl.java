package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.converter.DocumentConverter;
import cn.getech.base.demo.entity.LongTermChatMemoryEntity;
import cn.getech.base.demo.service.MemoryVectorService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 11030
 */
@Service
public class MemoryVectorServiceImpl implements MemoryVectorService {

    @Value("${spring.ai.memory.long-term.similarity-top-k:5}")
    private int longTermTopK;

    @Resource(name = "qwenChatClient")
    private ChatClient qwenChatClient;

    @Resource(name = "memoryQwenChatClient")
    private ChatClient memoryQwenChatClient;

    @Autowired
    private VectorStore vectorStore;

    @Override
    public String doChatHierarchical(String msg, String conversationId) {
        return memoryQwenChatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Override
    public void addMemory(String msg) {
        LongTermChatMemoryEntity longTermChatMemoryEntity = LongTermChatMemoryEntity.builder()
                .content(msg)
                .conversationId("123456")
                .createTime(String.valueOf(System.currentTimeMillis()))
                .memoryType("LONG_TERM")
                .build();

        Document doc = DocumentConverter.toDocument(
                longTermChatMemoryEntity,
                LongTermChatMemoryEntity::getContent,
                e -> Map.of(
                        "conversationId", longTermChatMemoryEntity.getConversationId(),
                        "createTime", longTermChatMemoryEntity.getCreateTime(),
                        "memoryType", longTermChatMemoryEntity.getMemoryType()));
        vectorStore.add(List.of(doc));
    }

    @Override
    public List<LongTermChatMemoryEntity> getMemory(String msg) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(msg)
                .topK(longTermTopK)
                .similarityThreshold(0.7)
                .build();

        return vectorStore.similaritySearch(searchRequest)
                .stream()
                .map(e -> DocumentConverter.toEntity(e, (content, metadata) -> {
                    return LongTermChatMemoryEntity.builder()
                            .docId(e.getId())
                            .content(content)
                            .conversationId((String) metadata.get("conversationId"))
                            .createTime((String) metadata.get("createTime"))
                            .memoryType((String) metadata.get("memoryType"))
                            .build();
                })).collect(Collectors.toList());
    }

}
