package cn.getech.base.demo.controller;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentExportVO;
import cn.getech.base.demo.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static cn.getech.base.demo.enums.ExportTaskStatusEnum.PROCESSING;

@Slf4j
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@Validated
@Tag(name = "导出管理", description = "知识库文档导出功能")
public class ExportController {

    @Autowired
    private  ExportService exportService;

    @PostMapping("/start")
    @Operation(summary = "启动导出任务", description = "启动知识库文档导出任务")
    public ResponseEntity<Map<String, Object>> startExport(@Valid @RequestBody KnowledgeDocumentExportDto dto) {
        try {
            CompletableFuture<KnowledgeDocumentExportVO> exportFuture = exportService.startExportTask(dto);
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