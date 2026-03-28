package cn.example.base.demo.controller;

import cn.example.base.demo.param.dto.KnowledgeDocumentExportDto;
import cn.example.base.demo.param.vo.KnowledgeDocumentExportVO;
import cn.example.base.demo.service.KnowledgeExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static cn.example.base.demo.enums.ExportTaskStatusEnum.PROCESSING;

@Slf4j
@Tag(name = "知识库导出接口", description = "知识库导出接口")
@RequestMapping("/knowledgeExport")
@RestController
public class KnowledgeExportController {

    @Autowired
    private KnowledgeExportService knowledgeExportService;

    @PostMapping("/start")
    @Operation(summary = "启动导出任务", description = "启动导出任务")
    public ResponseEntity<Map<String, Object>> start(@Validated @RequestBody KnowledgeDocumentExportDto dto) {
        try {
            CompletableFuture<KnowledgeDocumentExportVO> exportFuture = knowledgeExportService.startExportTask(dto);
            KnowledgeDocumentExportVO result = exportFuture.get();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "导出任务已启动");
            response.put("data", Map.of(
                    "taskId", result.getTaskId(),
                    "status", PROCESSING.getId(),
                    "format", dto.getFormat(),
                    "compress", dto.isGzipFormat()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启动导出任务失败", e);

            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "导出任务启动失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}