package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.TextSplitterBuild;
import cn.example.base.demo.check.DetectTextSegmentCheck;
import cn.example.base.demo.check.TextSplitterCheck;
import cn.example.base.demo.dto.TextSegmentDto;
import cn.example.base.demo.factory.TextSplitterFactory;
import cn.example.base.demo.service.TextSplitterService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 文本分割服务实现类：提供智能文本分割功能，支持多种分割算法和自动文本类型检测
 * @author 11030
 */
@Slf4j
@Service
public class TextSplitterServiceImpl implements TextSplitterService {

    @Autowired
    private TextSplitterFactory textSplitterFactory;

    @Autowired
    private TextSplitterBuild textSplitterBuild;

    @Resource(name = "ragDocumentVectorStore")
    private VectorStore ragDocumentVectorStore;

    /**
     * 使用特定分割器分割文本
     */
    @Override
    public List<Document> split(String text, String algorithm, Map<String, Object> metadata) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        try {
            TextSplitterCheck.validateAlgorithm(algorithm);

            TextSplitter splitter = textSplitterFactory.getTextSplitter(algorithm);
            Document doc = textSplitterBuild.createDocument(text, metadata);
            List<Document> documents = textSplitterBuild.splitDocument(splitter, doc);
            documents = enrichDocuments(documents, algorithm);

            ragDocumentVectorStore.add(documents);
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 智能分割文本：实现三层智能分割方案
     */
    @Override
    public List<Document> intelligentSplit(String text, Map<String, Object> metadata) {
        List<Document> documents = new ArrayList<>();
        if (StrUtil.isBlank(text)) {
            return documents;
        }

        try {
            // 第一层：混合格式块拆分
            List<TextSegmentDto> segments = DetectTextSegmentCheck.detectTextSegments(text);
            long timestamp = System.currentTimeMillis();

            // 对每个分段进行处理
            for (int i = 0; i < segments.size(); i++) {
                TextSegmentDto segment = segments.get(i);
                String algorithm = segment.getAlgorithm();
                String segmentText = segment.getText();

                // 第二层：中文优化预处理
                String processedText = textSplitterBuild.preprocessChineseText(segmentText);

                // 第三层：多粒度分层分割
                List<String> chunks = textSplitterBuild.processMultiGranularitySplit(processedText, algorithm);

                // 转换为Document对象并丰富元数据
                List<Document> segmentDocuments = enrichIntelligentDocuments(chunks, metadata, algorithm, i, segments.size(), timestamp);
                documents.addAll(segmentDocuments);
            }

            ragDocumentVectorStore.add(documents);
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量分割文本
     */
    @Override
    public Map<String, List<Document>> batchSplit(Map<String, String> texts, String algorithm) {
        Map<String, List<Document>> results = new ConcurrentHashMap<>();
        if (CollUtil.isEmpty(texts)) {
            return results;
        }

        try {
            TextSplitterCheck.validateAlgorithm(algorithm);

            List<Document> documents = new ArrayList<>();
            TextSplitter splitter = textSplitterFactory.getTextSplitter(algorithm);
            texts.forEach((docId, text) -> processBatchSplit(results, documents, docId, text, splitter, algorithm));

            ragDocumentVectorStore.add(documents);
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理批量分割中的单个文档
     */
    private void processBatchSplit(Map<String, List<Document>> results, List<Document> allDocuments,
                                   String docId, String text, TextSplitter splitter, String algorithm) {
        try {
            if (StrUtil.isBlank(text)) {
                results.put(docId, Collections.emptyList());
                return;
            }

            Document inputDoc = textSplitterBuild.createDocument(text, Map.of("docId", docId));
            List<Document> documents = textSplitterBuild.splitDocument(splitter, inputDoc);
            List<Document> processedDocs = enrichDocuments(documents, algorithm);

            allDocuments.addAll(processedDocs);
            results.put(docId, processedDocs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 丰富文档元数据
     */
    private List<Document> enrichDocuments(List<Document> documents, String algorithm) {
        long timestamp = System.currentTimeMillis();
        return documents.stream()
                .map(doc -> {
                    Map<String, Object> docMetadata = new HashMap<>(doc.getMetadata());
                    docMetadata.put("splitAlgorithm", algorithm);
                    docMetadata.put("chunkSize", doc.getText().length());
                    docMetadata.put("createdAt", timestamp);
                    return new Document(doc.getText(), docMetadata);
                }).collect(Collectors.toList());
    }

    /**
     * 丰富智能分割文档元数据
     */
    private List<Document> enrichIntelligentDocuments(List<String> chunks, Map<String, Object> metadata, String algorithm,
                                                      int segmentIndex, int totalSegments, long timestamp) {
        List<Document> documents = new ArrayList<>();
        for (int j = 0; j < chunks.size(); j++) {
            String chunk = chunks.get(j);
            Document doc = textSplitterBuild.createDocument(chunk, metadata);
            Map<String, Object> docMetadata = new HashMap<>(doc.getMetadata());
            docMetadata.put("splitAlgorithm", algorithm);
            docMetadata.put("segmentIndex", segmentIndex);
            docMetadata.put("chunkIndex", j);
            docMetadata.put("totalSegments", totalSegments);
            docMetadata.put("totalChunks", chunks.size());
            docMetadata.put("chunkSize", chunk.length());
            docMetadata.put("createdAt", timestamp);
            documents.add(new Document(chunk, docMetadata));
        }
        return documents;
    }

}