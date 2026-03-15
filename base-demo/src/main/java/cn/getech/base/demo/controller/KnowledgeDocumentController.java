package cn.getech.base.demo.controller;

import cn.getech.base.demo.dto.KnowledgeDocumentDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.entity.KnowledgeDocument;
import cn.getech.base.demo.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/knowledge")
@Tag(name = "知识库管理", description = "知识库管理相关API")
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
    public void batchCreateDocuments(@Validated @RequestBody List<KnowledgeDocumentDto> list) {
        knowledgeDocumentService.batchCreateDocuments(list);
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
    @Operation(summary = "获取文档详情", description = "根据ID获取文档详情")
    public KnowledgeDocument getDocumentById(@Parameter(description = "文档ID", required = true) @PathVariable Long documentId) {
        return knowledgeDocumentService.getDocumentById(documentId);
    }

    @PostMapping("/documents/search")
    @Operation(summary = "关键词搜索文档", description = "根据关键词、分类、标签等条件搜索知识库文档")
    public Map<String, Object> searchDocument(@Valid @RequestBody KnowledgeDocumentSearchDto dto) {
        return knowledgeDocumentService.searchDocument(dto);
    }

    @GetMapping("/documents/similarity")
    @Operation(summary = "向量相似性搜索", description = "基于向量相似性搜索最相关的知识库文档")
    public Map<String, Object> similaritySearch(
            @Parameter(description = "搜索查询", required = true) @RequestParam String query,

            @Parameter(description = "返回结果数量限制，默认5") @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "结果数量不能小于1") @Max(value = 20, message = "结果数量不能大于20") int limit,

            @Parameter(description = "相似度阈值，默认0.6") @RequestParam(defaultValue = "0.6")
            @Min(value = 0, message = "阈值不能小于0") @Max(value = 1, message = "阈值不能大于1") double similarityThreshold) {
        return knowledgeDocumentService.similaritySearch(query, limit, similarityThreshold);
    }

    /**
     * 获取文档列表（分页）
     */
    @GetMapping("/documents")
    @Operation(summary = "获取文档列表", description = "分页获取知识库文档列表，支持按分类、状态过滤")
    public ResponseEntity<Map<String, Object>> getDocuments(
            @Parameter(description = "当前页码，默认1")
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于1")
            int page,

            @Parameter(description = "每页记录数，默认20")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "每页记录数不能小于1")
            @Max(value = 100, message = "每页记录数不能大于100")
            int size,

            @Parameter(description = "分类名称过滤")
            @RequestParam(required = false) String category,

            @Parameter(description = "状态过滤(1:启用,0:禁用,2:待审核,3:已删除)")
            @RequestParam(required = false) Integer status,

            @Parameter(description = "按优先级排序")
            @RequestParam(required = false) Boolean orderByPriority) {
        return null;
    }

}
