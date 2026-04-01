package cn.example.ai.demo.service;

import cn.example.ai.demo.param.dto.KnowledgeDocumentExportDto;
import cn.example.ai.demo.param.vo.KnowledgeDocumentExportVO;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.CompletableFuture;

public interface KnowledgeExportService {

    /**
     * 异步启动导出任务
     */
    CompletableFuture<KnowledgeDocumentExportVO> startExportTask(KnowledgeDocumentExportDto dto);

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    void cleanupTempFiles(String taskId);

}
