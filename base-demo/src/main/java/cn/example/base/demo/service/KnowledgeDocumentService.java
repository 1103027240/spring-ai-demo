package cn.example.base.demo.service;

import cn.example.base.demo.dto.*;
import cn.example.base.demo.entity.KnowledgeDocument;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
public interface KnowledgeDocumentService {

    List<Map<String, Object>> searchKnowledge(String query, int limit);

    void createDocument(KnowledgeDocumentDto dto);

    void batchCreateDocument(KnowledgeDocumentAddDto dto);

    void updateDocument(KnowledgeDocumentDto dto);

    void deleteDocument(Long documentId);

    KnowledgeDocument getDocument(Long documentId);

    CursorSearchVO<KnowledgeDocumentVO> search(KnowledgeDocumentSearchDto dto);

}

