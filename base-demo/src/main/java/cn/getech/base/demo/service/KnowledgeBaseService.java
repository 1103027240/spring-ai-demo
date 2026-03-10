package cn.getech.base.demo.service;

import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
public interface KnowledgeBaseService {

    List<Map<String, Object>> searchKnowledge(String query, int limit);

    void addKnowledgeDocument(String content, String title, String category, List<String> tags);

    void batchAddKnowledgeDocuments(List<Map<String, Object>> documents);

    void clearKnowledgeDocument();

}
