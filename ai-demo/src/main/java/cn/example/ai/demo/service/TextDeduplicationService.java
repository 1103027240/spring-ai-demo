package cn.example.ai.demo.service;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文本去重服务接口
 * @author 11030
 */
public interface TextDeduplicationService {

    List<Document> deduplicateImport(List<Document> documents);

    List<Document> deduplicateResults(List<Document> searchResults);

    List<Document> semanticDeduplicate(List<Document> documents);

}
