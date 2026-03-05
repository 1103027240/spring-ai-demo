package cn.getech.spring.ai.demo.service.impl;

import cn.getech.spring.ai.demo.service.TextDeduplicationService;
import cn.getech.spring.ai.demo.utils.CalculateUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class TextDeduplicationServiceImpl implements TextDeduplicationService {

    @Value("${rag.deduplication.similarity-threshold:0.95}")
    private double similarityThreshold;

    @Value("${rag.deduplication.enabled:true}")
    private boolean enabled;

    /**
     * 导入时去重
     */
    @Override
    public List<Document> deduplicateImport(List<Document> documents) {
        if (!enabled || CollUtil.isEmpty(documents)) {
            return documents;
        }

        Map<String, Document> uniqueDocs = new LinkedHashMap<>();
        for (Document doc : documents) {
            String content = doc.getText();
            String hashValue = CalculateUtils.calculateHash(content);

            if (!uniqueDocs.containsKey(hashValue)) {
                doc.getMetadata().put("hashValue", hashValue);
                doc.getMetadata().put("createdAt", LocalDateTime.now());
                uniqueDocs.put(hashValue, doc);
            } else {
                // 存在，目前不做处理
            }
        }
        return new ArrayList<>(uniqueDocs.values());
    }

    /**
     * 查询结果去重
     */
    @Override
    public List<Document> deduplicateResults(List<Document> searchResults) {
        if (!enabled || CollUtil.isEmpty(searchResults)) {
            return searchResults;
        }

        List<Document> uniqueDocs = new ArrayList<>();
        Set<String> hashSet = new HashSet<>();
        for (Document doc : searchResults) {
            String hashValue = (String) doc.getMetadata().get("hashValue");
            if (StrUtil.isBlank(hashValue)) {
                hashValue = CalculateUtils.calculateHash(doc.getText());
            }

            if (!hashSet.contains(hashValue)) {
                hashSet.add(hashValue);
                uniqueDocs.add(doc);
            }
        }
        return uniqueDocs;
    }

    /**
     * 基于语义相似度的去重
     */
    @Override
    public List<Document> semanticDeduplicate(List<Document> documents) {
        if (!enabled || documents.size() <= 1) {
            return documents;
        }

        List<Document> result = new ArrayList<>();
        result.add(documents.get(0));

        for (int i = 1; i < documents.size(); i++) {
            Document current = documents.get(i);
            boolean isDuplicate = false;

            for (Document existing : result) {
                float[] existingEmbedding = (float[]) existing.getMetadata().get("embedding");
                float[] currentEmbedding = (float[]) current.getMetadata().get("embedding");

                if (existingEmbedding != null && currentEmbedding != null) {
                    double similarity = calculateCosineSimilarity(existingEmbedding, currentEmbedding);
                    if (similarity > similarityThreshold) {
                        isDuplicate = true;
                        break;
                    }
                }
            }

            if (!isDuplicate) {
                result.add(current);
            }
        }

        return result;
    }

    /**
     * 计算余弦相似度
     * @param vector1
     * @param vector2
     * @return
     */
    private double calculateCosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

}