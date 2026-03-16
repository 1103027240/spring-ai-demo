package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.*;
import cn.getech.base.demo.entity.KnowledgeDocument;
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

    CursorSearchVO<KnowledgeDocumentVO> search(KnowledgeDocumentSearchDto dto);

}

