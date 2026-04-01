package cn.example.ai.demo.controller;

import cn.example.ai.demo.entity.KnowledgeDocument;
import cn.example.ai.demo.param.dto.KnowledgeDocumentAddDto;
import cn.example.ai.demo.param.dto.KnowledgeDocumentDto;
import cn.example.ai.demo.param.dto.KnowledgeDocumentSearchDto;
import cn.example.ai.demo.param.vo.CursorSearchVO;
import cn.example.ai.demo.param.vo.KnowledgeDocumentVO;
import cn.example.ai.demo.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge")
@Tag(name = "知识库接口", description = "知识库接口")
public class KnowledgeDocumentController {

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/documents")
    @Operation(summary = "创建知识库文档", description = "创建知识库文档")
    public void createDocument(@Validated @RequestBody KnowledgeDocumentDto dto) {
        knowledgeDocumentService.createDocument(dto);
    }

    @PostMapping("/documents/batch")
    @Operation(summary = "批量创建知识库文档", description = "批量创建知识库文档")
    public void batchCreateDocument(@Validated @RequestBody KnowledgeDocumentAddDto dto) {
        knowledgeDocumentService.batchCreateDocument(dto);
    }

    @PutMapping("/documents")
    @Operation(summary = "更新知识库文档", description = "更新知识库文档")
    public void updateDocument(@Validated @RequestBody KnowledgeDocumentDto dto) {
        knowledgeDocumentService.updateDocument(dto);
    }

    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "删除知识库文档", description = "删除知识库文档")
    public void deleteDocument(@Parameter(description = "文档ID", required = true) @PathVariable Long documentId) {
        knowledgeDocumentService.deleteDocument(documentId);
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "获取知识库文档详情", description = "获取知识库文档详情")
    public KnowledgeDocument getDocument(@Parameter(description = "文档ID", required = true) @PathVariable Long documentId) {
        return knowledgeDocumentService.getDocument(documentId);
    }

    @PostMapping("/documents/search")
    @Operation(summary = "向量 + 标量搜索知识库文档", description = "向量 + 标量搜索知识库文档")
    public CursorSearchVO<KnowledgeDocumentVO> search(@Validated @RequestBody KnowledgeDocumentSearchDto dto) {
        return knowledgeDocumentService.search(dto);
    }

}
