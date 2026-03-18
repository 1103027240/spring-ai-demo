package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.entity.ExportTask;
import java.io.IOException;

public interface ExportTaskService {

    ExportTask createExportTask(String taskId, KnowledgeDocumentExportDto dto);

    void updateTaskStatus(ExportTask exportTask, String status, String errorMessage);

    void cleanupTaskTempFiles(String taskId) throws IOException;

    void cleanupAllExpiredTempFiles() throws IOException;

}
