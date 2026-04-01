package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.converter.DocumentConverter;
import cn.example.ai.demo.entity.LongTermChatMemory;
import cn.example.ai.demo.service.AiVectorService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 11030
 */
@Service
public class AiVectorServiceImpl implements AiVectorService {

    @Value("${spring.ai.memory.long-term.similarity-top-k:5}")
    private int longTermTopK;

    @Resource(name = "ragDocumentVectorStore")
    private VectorStore ragDocumentVectorStore;

    @Override
    public void addVector(String msg) {
        LongTermChatMemory longTermChatMemory = LongTermChatMemory.builder()
                .content(msg)
                .conversationId("123456")
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
        ragDocumentVectorStore.add(List.of(doc));
    }

    @Override
    public List<LongTermChatMemory> getVector(String msg) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(msg)
                .topK(longTermTopK)
                .similarityThreshold(0.7)
                .build();

        return ragDocumentVectorStore.similaritySearch(searchRequest)
                .stream()
                .map(e -> DocumentConverter.toEntity(e, (content, metadata) ->
                        LongTermChatMemory.builder()
                                .docId(e.getId())
                                .content(content)
                                .conversationId((String) metadata.get("conversationId"))
                                .createTime((String) metadata.get("createTime"))
                                .memoryType((String) metadata.get("memoryType"))
                                .build())).collect(Collectors.toList());
    }

}
