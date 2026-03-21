package cn.getech.base.demo.build;

import cn.getech.base.demo.dto.KnowledgeDocumentExportDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.service.writer.CsvExportWriter;
import cn.getech.base.demo.service.writer.ExcelExportWriter;
import cn.getech.base.demo.service.writer.ExportWriter;
import cn.getech.base.demo.service.writer.JsonExportWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.zip.GZIPOutputStream;
import static cn.getech.base.demo.constant.FieldValueConstant.EXPORT_FILE_PREFIX;
import static cn.getech.base.demo.enums.CursorDirectionEnum.NEXT;
import static cn.hutool.core.date.DatePattern.PURE_DATETIME_FORMATTER;

@Component
public class knowledgeExportBuild {

    @Value("${export.task.base-path}")
    private String baseExportPath;

    /**
     * 生成文件名
     */
    public String generateFileName(KnowledgeDocumentExportDto dto, String taskId) {
        String timestamp = PURE_DATETIME_FORMATTER.format(LocalDateTime.now());
        String baseName = EXPORT_FILE_PREFIX + taskId + "_" + timestamp;
        return baseName + dto.getFileExtension();
    }

    /**
     * 准备导出文件
     */
    public Path prepareExportFile(String fileName) throws IOException {
        // 创建导出目录
        Path exportDir = Paths.get(baseExportPath);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        // 返回文件路径
        return exportDir.resolve(fileName);
    }

    /**
     * 创建导出写入器
     */
    public ExportWriter createExportWriter(Path filePath, KnowledgeDocumentExportDto dto) throws IOException {
        OutputStream outputStream = createOutputStream(filePath, dto);
        if (dto.isExcelFormat()) {
            return new ExcelExportWriter(outputStream, dto);
        } else if (dto.isCsvFormat()) {
            return new CsvExportWriter(outputStream, dto);
        } else if (dto.isJsonFormat()) {
            return new JsonExportWriter(outputStream, dto);
        } else {
            throw new IllegalArgumentException("不支持的导出格式: " + dto.getFormat());
        }
    }

    /**
     * 构建批处理搜索请求
     */
    public KnowledgeDocumentSearchDto buildSearchForBatch(KnowledgeDocumentExportDto dto, String nextCursor) {
        KnowledgeDocumentSearchDto batchDto = dto.getSearchCondition();
        batchDto.setPageSize(dto.getBatchSize());
        batchDto.setCursorDirection(NEXT.getId());
        batchDto.setNextCursor(nextCursor);
        return batchDto;
    }

    /**
     * 创建输出流（支持GZIP压缩）
     */
    private OutputStream createOutputStream(Path filePath, KnowledgeDocumentExportDto dto) throws IOException {
        OutputStream fileStream = Files.newOutputStream(filePath);
        if (dto.isGzipFormat()) {
            return new GZIPOutputStream(fileStream, true) {
                {
                    this.def.setLevel(dto.getCompressionLevel());
                }
            };
        }
        return fileStream;
    }

    /**
     * 计算压缩比
     */
    public Double calculateCompressionRatio(Path filePath, KnowledgeDocumentExportDto exportDto) {
        if (!exportDto.isGzipFormat()) {
            return 0.0;
        }
        return null;
    }

}
