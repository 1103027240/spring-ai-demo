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

    void createDocument(KnowledgeDocumentDto dto);

    void batchCreateDocument(KnowledgeDocumentAddDto dto);

    void updateDocument(KnowledgeDocumentDto dto);

    void deleteDocument(Long documentId);

    KnowledgeDocument getDocument(Long documentId);

    CursorSearchVO<KnowledgeDocumentVO> search(KnowledgeDocumentSearchDto dto);

}

