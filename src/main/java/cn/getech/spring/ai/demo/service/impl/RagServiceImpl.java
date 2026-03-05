package cn.getech.spring.ai.demo.service.impl;

import cn.getech.spring.ai.demo.service.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    @Autowired
    private TextSplitterService textSplitterService;

    @Autowired
    private TextDeduplicationService textDeduplicationService;

    @Autowired
    private TextRerankingService textRerankingService;

    @Autowired
    private QueryRewriteService queryRewriteService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Value("${rag.rerank.top-k:5}")
    private int topK;

    /**
     * 处理文档并存储
     */
    @Override
    public void addDocument(String text, String splitAlgorithm, Map<String, Object> metadata) {
        // 1. 文本分割
        List<Document> chunks = textSplitterService.intelligentSplit(text, metadata);

        // 2. 去重处理
        List<Document> uniqueResults = textDeduplicationService.deduplicateImport(chunks);

        // 3. 存储到向量数据库
        vectorStoreService.storeDocuments(uniqueResults);
    }

    /**
     * 检索
     */
    @Override
    public List<Document> search(String query) {
        // 1. 查询改写
        List<String> rewrittenQueries = queryRewriteService.rewriteQuery(query);

        // 2. 执行搜索
        List<Document> allResults = new ArrayList<>();
        for (String rewrittenQuery : rewrittenQueries) {
            List<Document> results = vectorStoreService.search(rewrittenQuery, topK * 2);
            allResults.addAll(results);
        }

        // 3. 去重
        List<Document> deduplicateResults = textDeduplicationService.deduplicateResults(allResults);

        // 4. 重排
        return textRerankingService.enhancedRerank(deduplicateResults, query);
    }

    /**
     * 分页搜索
     */
    @Override
    public Page<Document> pageSearch(String query, int page, int size) {
        // 1. 查询改写
        List<String> rewrittenQueries = queryRewriteService.rewriteQuery(query);

        // 2. 执行搜索
        List<Document> allResults = new ArrayList<>();
        for (String rewrittenQuery : rewrittenQueries) {
            List<Document> results = vectorStoreService.search(rewrittenQuery, 100); // 获取较多结果用于分页
            allResults.addAll(results);
        }

        // 3. 去重
        List<Document> deduplicateResults = textDeduplicationService.deduplicateResults(allResults);

        // 4. 重排
        List<Document> rerankedResults = textRerankingService.enhancedRerank(deduplicateResults, query);

        // 5. 分页
        Page<Document> resultPage = new Page<>(page, size);
        resultPage.setRecords(rerankedResults);
        resultPage.setTotal(rerankedResults.size());
        return resultPage;
    }

    /**
     * 批量处理文档
     */
    @Override
    public Map<String, Object> batchProcessDocuments(Map<String, String> documents, String splitAlgorithm) {
        Map<String, Object> result = new HashMap<>();
        int totalChunks = 0;
        int uniqueChunks = 0;

        for (Map.Entry<String, String> entry : documents.entrySet()) {
            String docId = entry.getKey();
            String content = entry.getValue();

            // 分割
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("docId", docId);
            metadata.put("source", "batch_import");
            metadata.put("createdAt", new Date().toString());

            List<Document> chunks = textSplitterService.split(content, splitAlgorithm, metadata);
            totalChunks += chunks.size();

            // 去重
            List<Document> unique = textDeduplicationService.deduplicateImport(chunks);
            uniqueChunks += unique.size();

            // 存储
            vectorStoreService.storeDocuments(unique);

            result.put(docId + "_original_chunks", chunks.size());
            result.put(docId + "_unique_chunks", unique.size());
        }

        result.put("total_original_chunks", totalChunks);
        result.put("total_unique_chunks", uniqueChunks);
        result.put("duplicates_removed", totalChunks - uniqueChunks);
        return result;
    }

    /**
     * 获取检索结果并格式化
     */
    @Override
    public Map<String, Object> searchWithAnalysis(String query) {
        List<Document> results = search(query);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("originalQuery", query);
        response.put("resultCount", results.size());

        List<Map<String, Object>> formattedResults = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            Map<String, Object> docInfo = new HashMap<>();
            docInfo.put("rank", i + 1);
            docInfo.put("content", doc.getText());
            docInfo.put("score", doc.getMetadata().get("similarity_score"));
            docInfo.put("rerankScore", doc.getMetadata().get("rerankScore"));
            docInfo.put("metadata", doc.getMetadata());
            formattedResults.add(docInfo);
        }

        response.put("results", formattedResults);

        // 分析信息
        Map<String, Object> analysis = new HashMap<>();
        if (!results.isEmpty()) {
            analysis.put("topScore", results.get(0).getMetadata().get("similarity_score"));

            double avgScore = results.stream()
                    .mapToDouble(doc -> (Double) doc.getMetadata().get("similarity_score"))
                    .average()
                    .orElse(0.0);
            analysis.put("averageScore", avgScore);
        }

        response.put("analysis", analysis);
        return response;
    }

}
