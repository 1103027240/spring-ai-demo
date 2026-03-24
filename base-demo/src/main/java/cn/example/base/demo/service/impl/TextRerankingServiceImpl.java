package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.TextRerankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 11030
 */
@Slf4j
@Service
public class TextRerankingServiceImpl implements TextRerankingService {

    @Value("${rag.rerank.enabled:true}")
    private boolean enabled;

    @Value("${rag.rerank.top-k:5}")
    private int topK;

    /**
     * 基础重排：基于相似度分数
     */
    @Override
    public List<Document> rerankByScore(List<Document> documents) {
        if (!enabled) {
            return documents.stream().limit(topK).collect(Collectors.toList());
        }

        return documents.stream()
                .sorted(Comparator.comparingDouble(this::extractScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * 增强重排：结合多种因素
     */
    @Override
    public List<Document> enhancedRerank(List<Document> documents, String query) {
        if (!enabled) {
            return documents.stream().limit(topK).collect(Collectors.toList());
        }

        return documents.stream()
                .map(doc -> {
                    double score = calculateEnhancedScore(doc, query);
                    doc.getMetadata().put("originalScore", score);
                    return doc;
                })
                .sorted(Comparator.comparingDouble(this::extractScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double calculateEnhancedScore(Document doc, String query) {
        double baseScore = extractScore(doc);

        // 1. 长度归一化
        String content = doc.getText();
        double lengthScore = Math.min(content.length() / 500.0, 1.0);

        // 2. 关键词匹配
        double keywordScore = calculateKeywordMatch(content, query);

        // 3. 时效性
        double recencyScore = calculateRecencyScore(doc);

        // 4. 权威性
        double authorityScore = calculateAuthorityScore(doc);

        // 加权综合评分
        return baseScore * 0.4 +
                lengthScore * 0.2 +
                keywordScore * 0.2 +
                recencyScore * 0.1 +
                authorityScore * 0.1;
    }

    private double extractScore(Document doc) {
        Object score = doc.getMetadata().get("originalScore");
        if (score instanceof Number) {
            return ((Number) score).doubleValue();
        }
        return 0.0;
    }

    private double calculateKeywordMatch(String content, String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();

        int matches = 0;
        for (String word : queryWords) {
            if (word.length() > 2 && contentLower.contains(word)) {
                matches++;
            }
        }

        return (double) matches / queryWords.length;
    }

    private double calculateRecencyScore(Document doc) {
        Object dateObj = doc.getMetadata().get("createdAt");
        if (dateObj != null) {
            return 0.8; // 这里可以基于实际日期计算
        }
        return 0.5;
    }

    private double calculateAuthorityScore(Document doc) {
        Object source = doc.getMetadata().get("source");
        if (source instanceof String) {
            String sourceStr = (String) source;
            if (sourceStr.contains("official") || sourceStr.contains("wiki")) {
                return 0.9;
            } else if (sourceStr.contains("blog")) {
                return 0.7;
            } else {
                return 0.5;
            }
        }
        return 0.5;
    }
}
