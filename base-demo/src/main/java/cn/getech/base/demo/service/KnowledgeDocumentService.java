package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.KnowledgeDocumentAddDto;
import cn.getech.base.demo.dto.KnowledgeDocumentDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.entity.KnowledgeDocument;
import jakarta.validation.constraints.Max;

import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
public interface KnowledgeDocumentService {

    List<Map<String, Object>> searchKnowledge(String query, int limit);

    KnowledgeDocument createDocument(KnowledgeDocumentDto dto);

    void batchCreateDocuments(KnowledgeDocumentAddDto dto);

    void updateDocument(KnowledgeDocumentDto dto);

    void deleteDocument(Long documentId);

    KnowledgeDocument getDocumentById(Long documentId);

    Map<String, Object> searchDocument(KnowledgeDocumentSearchDto dto);

    Map<String, Object> similaritySearch(String query, int limit, String cursor, String cursorDirection, double similarityThreshold);

    Map<String, Object> similaritySearch(String query, int limit, String cursor, String cursorDirection, double similarityThreshold, Boolean enableHybridMode);

}

