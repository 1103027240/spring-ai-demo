package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.entity.ExportTask;
import java.io.IOException;
import java.nio.file.Path;

public interface ExportTaskService {

    ExportTask createExportTask(String taskId, KnowledgeDocumentExportDto dto);

    void updateTaskStatus(ExportTask exportTask, String status, String fileName, Path filePath, String errorMessage);

    void cleanupTaskTempFiles(String taskId) throws IOException;

    void cleanupAllExpiredTempFiles() throws IOException;

}
