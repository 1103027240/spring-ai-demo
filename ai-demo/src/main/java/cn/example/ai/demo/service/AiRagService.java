package cn.example.ai.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * RAG服务接口
 * @author 11030
 */
public interface AiRagService {

    void addDocument(String text, String splitAlgorithm, Map<String, Object> metadata);

    List<Document> search(String query);

    String advanceSearchV1(String msg);

    String advanceSearchV2(String msg);

    Page<Document> pageSearch(String query, int page, int size);

    Map<String, Object> batchProcessDocuments(Map<String, String> documents, String splitAlgorithm);

    Map<String, Object> searchWithAnalysis(String query);

}
