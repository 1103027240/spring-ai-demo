package cn.example.base.demo.service;

import cn.example.base.demo.entity.KnowledgeDocument;
import cn.example.base.demo.param.dto.KnowledgeDocumentAddDto;
import cn.example.base.demo.param.dto.KnowledgeDocumentDto;
import cn.example.base.demo.param.dto.KnowledgeDocumentSearchDto;
import cn.example.base.demo.param.vo.CursorSearchVO;
import cn.example.base.demo.param.vo.KnowledgeDocumentVO;

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

