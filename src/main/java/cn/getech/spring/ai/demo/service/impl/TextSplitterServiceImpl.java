package cn.getech.spring.ai.demo.service.impl;

import cn.getech.spring.ai.demo.dto.TextAnalysisDto;
import cn.getech.spring.ai.demo.enums.SplitterTypeEnum;
import cn.getech.spring.ai.demo.factory.TextSplitterFactory;
import cn.getech.spring.ai.demo.service.TextSplitterService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 文本分割服务实现类：提供智能文本分割功能，支持多种分割算法和自动文本类型检测
 * @author 11030
 */
@Slf4j
@Service
public class TextSplitterServiceImpl implements TextSplitterService {

    /** 文本分割器默认算法 **/
    @Value("${rag.text-splitting.default-strategy:recursive}")
    private String defaultStrategy;

    @Autowired
    private TextSplitterFactory textSplitterFactory;

    /**
     * 使用特定分割器分割文本
     */
    @Override
    public List<Document> split(String text, String algorithm, Map<String, Object> metadata) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        try {
            validateAlgorithm(algorithm);
            TextSplitter splitter = textSplitterFactory.getTextSplitter(algorithm);
            Document inputDoc = createDocument(text, metadata);
            List<Document> documents = splitDocument(splitter, inputDoc);
            return enrichDocuments(documents, algorithm);
        } catch (Exception e) {
            log.error("Error during split with algorithm {}: {}", algorithm, e.getMessage(), e);
            return Collections.singletonList(createDocument(text, metadata));
        }
    }

    /**
     * 智能分割文本：自动选择最合适的分割器
     */
    @Override
    public List<Document> intelligentSplit(String text, Map<String, Object> metadata) {
        if (StrUtil.isBlank(text)) {
            return Collections.emptyList();
        }

        try {
            TextAnalysisDto analysis = analyzeText(text);
            String algorithm = selectSplitterByAnalysis(analysis);
            TextSplitter splitter = textSplitterFactory.getTextSplitterOrDefault(algorithm);

            Document inputDoc = createDocument(text, metadata);
            List<Document> documents = splitDocument(splitter, inputDoc);
            return enrichDocuments(documents, analysis, algorithm);
        } catch (Exception e) {
            log.error("Error during intelligent split: {}", e.getMessage(), e);
            return Collections.singletonList(createDocument(text, metadata));
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
            validateAlgorithm(algorithm);
            TextSplitter splitter = textSplitterFactory.getTextSplitter(algorithm);
            texts.forEach((docId, text) -> processBatchSplit(results, docId, text, splitter, algorithm));
        } catch (Exception e) {
            log.error("Error during batch split with algorithm {}: {}", algorithm, e.getMessage(), e);
            texts.keySet().forEach(docId -> results.put(docId, Collections.emptyList()));
        }
        return results;
    }

    /**
     * 处理批量分割中的单个文档
     */
    private void processBatchSplit(Map<String, List<Document>> results, String docId, String text, TextSplitter splitter, String algorithm) {
        try {
            if (StrUtil.isBlank(text)) {
                results.put(docId, Collections.emptyList());
                return;
            }
            Document inputDoc = createDocument(text, Map.of("docId", docId));
            List<Document> documents = splitDocument(splitter, inputDoc);
            List<Document> processedDocs = enrichDocuments(documents, algorithm);
            results.put(docId, processedDocs);
        } catch (Exception e) {
            log.error("Failed to batch split document: {}", docId, e);
            results.put(docId, Collections.emptyList());
        }
    }

    /**
     * 验证算法参数
     */
    private void validateAlgorithm(String algorithm) {
        if (StrUtil.isBlank(algorithm)) {
            throw new IllegalArgumentException("Algorithm cannot be blank");
        }
    }

    /**
     * 分析文本特征
     */
    private TextAnalysisDto analyzeText(String text) {
        return TextAnalysisDto.builder()
                .language(detectLanguage(text))
                .type(detectTextType(text))
                .length(text.length())
                .lineCount(text.split("\n").length)
                .build();
    }

    /**
     * 根据分析结果选择分割器
     */
    private String selectSplitterByAnalysis(TextAnalysisDto analysis) {
        // 优先考虑语言特性
        if ("chinese".equals(analysis.getLanguage())) {
            return SplitterTypeEnum.CHINESE.getId();
        }
        // 然后考虑文本类型
        return Optional.ofNullable(SplitterTypeEnum.findByType(analysis.getType()))
                .map(SplitterTypeEnum::getId)
                .orElse(defaultStrategy);
    }

    /**
     * 创建文档对象
     */
    private Document createDocument(String text, Map<String, Object> metadata) {
        return new Document(text, metadata != null ? metadata : new HashMap<>());
    }

    /**
     * 分割文档
     */
    private List<Document> splitDocument(TextSplitter splitter, Document document) {
        return splitter.apply(Collections.singletonList(document));
    }

    /**
     * 丰富文档元数据（指定算法）
     */
    private List<Document> enrichDocuments(List<Document> documents, String algorithm) {
        long timestamp = System.currentTimeMillis();
        return documents.stream().map(doc -> {
            Map<String, Object> docMetadata = new HashMap<>(doc.getMetadata());
            docMetadata.put("splitAlgorithm", algorithm);
            docMetadata.put("chunkSize", doc.getText().length());
            docMetadata.put("timestamp", timestamp);
            return new Document(doc.getText(), docMetadata);
        }).collect(Collectors.toList());
    }

    /**
     * 丰富文档元数据（智能分割）
     */
    private List<Document> enrichDocuments(List<Document> documents, TextAnalysisDto analysis, String algorithm) {
        long timestamp = System.currentTimeMillis();
        return IntStream.range(0, documents.size())
                .mapToObj(i -> {
                    Document doc = documents.get(i);
                    Map<String, Object> docMetadata = new HashMap<>(doc.getMetadata());
                    docMetadata.put("splitAlgorithm", algorithm);
                    docMetadata.put("detectedType", analysis.getType());
                    docMetadata.put("detectedLanguage", analysis.getLanguage());
                    docMetadata.put("chunkSize", doc.getText().length());
                    docMetadata.put("chunkIndex", i);
                    docMetadata.put("totalChunks", documents.size());
                    docMetadata.put("timestamp", timestamp);
                    return new Document(doc.getText(), docMetadata);
                }).collect(Collectors.toList());
    }

    /**
     * 检测语言类型
     */
    private String detectLanguage(String text) {
        if (StrUtil.isBlank(text)) {
            return "unknown";
        }
        long chineseCount = text.chars()
                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)
                .count();
        double chineseRatio = (double) chineseCount / text.length();
        return chineseRatio > 0.3 ? "chinese" : "english";
    }

    /**
     * 检测文本类型
     */
    private String detectTextType(String text) {
        if (StrUtil.isBlank(text)) {
            return "plain";
        }
        String trimmed = text.trim();
        if (isMarkdown(trimmed)) {
            return "markdown";
        }
        if (isHtml(trimmed)) {
            return "html";
        }
        if (JSONUtil.isTypeJSON(trimmed)) {
            return "json";
        }
        if (isCode(trimmed)) {
            return "code";
        }
        return "plain";
    }

    /**
     * 判断是否为 Markdown
     */
    private boolean isMarkdown(String text) {
        return text.contains("# ") || text.contains("## ") || text.contains("### ") ||
                text.contains("```") || text.contains("* ") || text.contains("1. ") || text.contains("> ");
    }

    /**
     * 判断是否为 HTML
     */
    private boolean isHtml(String text) {
        return text.contains("<html") || text.contains("<div") || text.contains("<p>") ||
                text.contains("<body") || text.contains("<!DOCTYPE");
    }

    /**
     * 判断是否为代码
     */
    private boolean isCode(String text) {
        return text.contains("public class") || text.contains("function ") ||
                text.contains("import ") || text.contains("def ") ||
                text.contains("package ") || text.contains("public static") ||
                text.contains("int ") || text.contains("String ") ||
                text.contains("System.out.println") || text.contains("```");
    }

}