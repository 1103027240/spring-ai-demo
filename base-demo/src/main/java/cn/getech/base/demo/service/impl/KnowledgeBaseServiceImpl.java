package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static cn.getech.base.demo.constant.FieldConstant.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Resource(name = "customerKnowledgeVectorStore")
    private VectorStore customerKnowledgeVectorStore;

    @Value("${app.knowledge.similarity-threshold:0.7}")
    private double similarityThreshold;

    /**
     * 搜索相关知识
     */
    @Override
    public List<Map<String, Object>> searchKnowledge(String query, int limit) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(limit)
                    .similarityThreshold(similarityThreshold)
                    .build();

            List<Document> documents = customerKnowledgeVectorStore.similaritySearch(request);
            return documents.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>(doc.getMetadata());
                        result.put(CONTENT, doc.getText());
                        return result;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("向量搜索失败，尝试文本搜索", e);
            return searchByText(query);
        }
    }

    /**
     * 添加知识文档
     */
    @Override
    public void addKnowledgeDocument(String content, String title, String category, List<String> tags) {
        Document document = new Document(content);
        document.getMetadata().put(TITLE, title);
        document.getMetadata().put(CATEGORY, category);
        document.getMetadata().put(TAGS, String.join(",", tags));
        document.getMetadata().put(CREATE_TIME, System.currentTimeMillis());
        customerKnowledgeVectorStore.add(List.of(document));
    }

    /**
     * 批量添加知识文档
     */
    @Override
    public void batchAddKnowledgeDocuments(List<Map<String, Object>> documents) {
        List<Document> docs = documents.stream()
                .map(e -> {
                    Document document = new Document((String) e.get(CONTENT));
                    document.getMetadata().putAll(e);
                    return document;
                }).collect(Collectors.toList());
        customerKnowledgeVectorStore.add(docs);
    }

    /**
     * 清除知识库
     */
    @Override
    public void clearKnowledgeDocument() {
        try {
            if (customerKnowledgeVectorStore instanceof MilvusVectorStore) {
                customerKnowledgeVectorStore.delete(List.of());
            }
        } catch (Exception e) {
            throw new RuntimeException("清空知识库失败", e);
        }
    }

    /**
     * 文本搜索（回退方案）
     */
    private List<Map<String, Object>> searchByText(String query) {
        // 实现简单的文本匹配，这里可以扩展为数据库全文搜索
        List<Map<String, Object>> results = new ArrayList<>();

        // 简化实现，实际应该从数据库查询
        if (query.contains("退货")) {
            results.add(createKnowledgeResult("退货政策", "7天无理由退货，商品需保持完好"));
        } else if (query.contains("发货") || query.contains("物流")) {
            results.add(createKnowledgeResult("发货时间", "工作日16:00前下单当天发货"));
        } else if (query.contains("保修")) {
            results.add(createKnowledgeResult("商品保修", "所有商品享受一年保修服务"));
        } else if (query.contains("支付")) {
            results.add(createKnowledgeResult("支付方式", "支持支付宝、微信支付、银联支付"));
        } else {
            results.add(createKnowledgeResult("客服帮助", "您好，请问有什么可以帮您？"));
        }

        return results;
    }

    private Map<String, Object> createKnowledgeResult(String title, String content) {
        Map<String, Object> result = new HashMap<>();
        result.put(TITLE, title);
        result.put(CONTENT, content);
        result.put(SCORE, 0.9);
        return result;
    }

}
