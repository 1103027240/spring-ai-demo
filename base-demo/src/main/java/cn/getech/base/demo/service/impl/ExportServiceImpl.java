package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.ExportBuild;
import cn.getech.base.demo.check.ExportCheck;
import cn.getech.base.demo.dto.*;
import cn.getech.base.demo.entity.ExportTask;
import cn.getech.base.demo.service.ExportService;
import cn.getech.base.demo.service.ExportTaskService;
import cn.getech.base.demo.service.KnowledgeDocumentService;
import cn.getech.base.demo.service.writer.ExportWriter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import static cn.getech.base.demo.enums.ExportTaskStatusEnum.*;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private ExportTaskService exportTaskService;

    @Autowired
    private ExportBuild exportBuild;

    @Autowired
    private ExportCheck exportCheck;

    @Async("exportTaskExecutor")
    @Override
    public CompletableFuture<KnowledgeDocumentExportVO> startExportTask(KnowledgeDocumentExportDto dto) {
        String taskId = IdUtil.fastSimpleUUID();
        ExportTask exportTask = null;

        try {
            log.info("开始导出任务: taskId={}, format={}", taskId, dto.getFormat());

            // 1. 创建导出任务
            exportTask = exportTaskService.createExportTask(taskId, dto);

            // 准备导出文件
            String fileName = exportBuild.generateFileName(dto, taskId);
            Path filePath = exportBuild.prepareExportFile(fileName);

            // 2. 执行导出
            long startTime = System.currentTimeMillis();
            executeExport(dto, filePath, exportTask);
            long endTime = System.currentTimeMillis();

            // 计算文件大小
            long fileSize = Files.size(filePath);

            // 3. 更新任务状态为已完成
            exportTaskService.updateTaskStatus(exportTask, COMPLETED.getId(), null);

            // 4. 构建返回结果
            KnowledgeDocumentExportVO result = KnowledgeDocumentExportVO.builder()
                    .taskId(taskId)
                    .filePath(filePath.toString())
                    .fileName(fileName)
                    .fileSize(fileSize)
                    .status(COMPLETED.getId())
                    .duration(endTime - startTime)
                    .format(dto.getFormat())
                    .compressionRatio(exportBuild.calculateCompressionRatio(filePath, dto))
                    .downloadUrl("/api/export/download/" + taskId)
                    .build();

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("导出任务失败: taskId={}", taskId, e);

            // 3、更新任务状态为失败
            exportTaskService.updateTaskStatus(exportTask, FAILED.getId(), e.getMessage());

            // 4、清理临时文件
            cleanupTempFiles(taskId);

            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 执行导出
     */
    public void executeExport(KnowledgeDocumentExportDto dto, Path filePath, ExportTask exportTask) throws IOException {
        AtomicLong totalExported = new AtomicLong(0);
        boolean hasNext = true;
        String nextCursor = null;

        // 准备写入器
        try (ExportWriter writer = exportBuild.createExportWriter(filePath, dto)) {
            while (hasNext && !exportCheck.isTaskCancelled(exportTask.getTaskId())) {
                // 构建搜索请求
                KnowledgeDocumentSearchDto searchDto = exportBuild.buildSearchForBatch(dto, nextCursor);

                // 1、执行搜索
                CursorSearchVO<KnowledgeDocumentVO> searchResult = knowledgeDocumentService.search(searchDto);
                if (CollUtil.isEmpty(searchResult.getData())) {
                    break;
                }

                // 2、写入当前批次数据
                writer.writeBatch(searchResult.getData());

                // 3、更新任务状态为处理中，首次写入更新任务状态
                if (totalExported.getAndIncrement() == 0) { 
                    exportTaskService.updateTaskStatus(exportTask, PROCESSING.getId(), null);
                }

                // 检查是否还有更多数据
                hasNext = Boolean.TRUE.equals(searchResult.getHasNext()) && searchResult.getNextCursor() != null;
                nextCursor = searchResult.getNextCursor();

                // 短暂休眠，避免CPU过高
                if (hasNext) {
                    Thread.sleep(50);
                }
            }

            // 4、完成写入
            writer.finish();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("导出任务被中断", e);
        }
    }

    /**
     * 清理临时文件
     */
    @Override
    public void cleanupTempFiles(String taskId) {
        log.info("开始清理临时文件: taskId={}", taskId);
        try {
            // 1. 清理特定任务的临时文件
            if (StrUtil.isNotBlank(taskId)) {
                exportTaskService.cleanupTaskTempFiles(taskId);
            } else {
                // 2. 清理所有过期临时文件
                exportTaskService.cleanupAllExpiredTempFiles();
            }
        } catch (Exception e) {
            log.error("清理临时文件失败: taskId={}", taskId, e);
        }
    }

}
