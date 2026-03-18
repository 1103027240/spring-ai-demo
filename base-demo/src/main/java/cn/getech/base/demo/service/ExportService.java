package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentExportVO;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.CompletableFuture;

public interface ExportService {

    /**
     * 异步启动导出任务
     */
    CompletableFuture<KnowledgeDocumentExportVO> startExportTask(KnowledgeDocumentExportDto dto);

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    void cleanupTempFiles(String taskId);

}
