//package cn.getech.base.demo.controller;
//
//import cn.getech.base.demo.entity.ExportTask;
//import cn.getech.base.demo.service.ExportService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import javax.servlet.http.HttpServletResponse;
//import javax.validation.Valid;
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/export")
//@RequiredArgsConstructor
//@Validated
//@Tag(name = "导出管理", description = "知识库文档导出功能")
//public class ExportController {
//
//    @Autowired
//    private  ExportService exportService;
//
//    @PostMapping("/create")
//    @Operation(summary = "创建导出任务", description = "创建异步导出任务")
//    public ResponseEntity<ExportTask> createExportTask(
//            @Valid @RequestBody ExportRequest request,
//            @RequestHeader("X-User-Id") String userId,
//            @RequestHeader("X-User-Name") String userName) {
//
//        ExportTask task = exportService.createExportTask(request, userId, userName);
//        return ResponseEntity.ok(task);
//    }
//
//    @GetMapping("/stream")
//    @Operation(summary = "流式导出", description = "直接流式导出文档")
//    public void streamExport(@Valid ExportRequest request, HttpServletResponse response) throws Exception {
//        exportService.streamExport(request, response);
//    }
//
//    @GetMapping("/task/{taskId}")
//    @Operation(summary = "获取导出任务", description = "根据任务ID获取导出任务详情")
//    public ResponseEntity<ExportTask> getExportTask(@PathVariable String taskId) {
//        ExportTask task = exportService.getExportTask(taskId);
//        if (task == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(task);
//    }
//
//    @GetMapping("/progress/{taskId}")
//    @Operation(summary = "获取导出进度", description = "获取导出任务的执行进度")
//    public ResponseEntity<ExportProgress> getExportProgress(@PathVariable String taskId) {
//        ExportProgress progress = exportService.getExportProgress(taskId);
//        if (progress == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(progress);
//    }
//
//    @GetMapping("/download/{taskId}")
//    @Operation(summary = "下载导出文件", description = "下载已完成的导出文件")
//    public ResponseEntity<?> downloadExportFile(@PathVariable String taskId) throws Exception {
//        return exportService.downloadExportFile(taskId);
//    }
//
//    @GetMapping("/tasks")
//    @Operation(summary = "获取用户导出任务列表", description = "获取用户的所有导出任务")
//    public ResponseEntity<List<ExportTask>> getUserExportTasks(
//            @RequestHeader("X-User-Id") String userId,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "20") int size) {
//
//        List<ExportTask> tasks = exportService.getUserExportTasks(userId, page, size);
//        return ResponseEntity.ok(tasks);
//    }
//
//    @PostMapping("/cancel/{taskId}")
//    @Operation(summary = "取消导出任务", description = "取消进行中的导出任务")
//    public ResponseEntity<Boolean> cancelExportTask(
//            @PathVariable String taskId,
//            @RequestHeader("X-User-Id") String userId) {
//
//        boolean result = exportService.cancelExportTask(taskId, userId);
//        return ResponseEntity.ok(result);
//    }
//
//    @PostMapping("/retry/{taskId}")
//    @Operation(summary = "重试导出任务", description = "重新执行失败的导出任务")
//    public ResponseEntity<Boolean> retryExportTask(
//            @PathVariable String taskId,
//            @RequestHeader("X-User-Id") String userId) {
//
//        boolean result = exportService.retryExportTask(taskId, userId);
//        return ResponseEntity.ok(result);
//    }
//
//}