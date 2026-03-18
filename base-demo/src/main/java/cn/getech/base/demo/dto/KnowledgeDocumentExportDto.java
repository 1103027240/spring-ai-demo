package cn.getech.base.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serializable;
import java.util.List;
import static cn.getech.base.demo.enums.ExportFormatEnum.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeDocumentExportDto implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========== 搜索条件 ==========

    @NotNull(message = "搜索条件不能为空")
    @Schema(description = "搜索条件", example = "搜索条件", required = true)
    private KnowledgeDocumentSearchDto searchConditionDto;

    // ========== 导出配置 ==========

    @NotNull(message = "导出格式不能为空")
    @Schema(description = "导出格式: JSON, CSV, EXCEL, GZIP", example = "EXCEL", required = true)
    private String format = "EXCEL";

    @Schema(description = "导出字段列表，为空则导出所有字段")
    private List<String> exportFields = List.of(
            "docId", "title", "content", "summary",
            "categoryId", "keywords", "tags", "source",
            "author", "score", "createTime"
    );

    @Schema(description = "是否压缩", example = "默认true")
    private Boolean compress = true;

    @Schema(description = "压缩级别(1-9)", example = "默认6")
    private Integer compressionLevel = 6;

    @Schema(description = "是否包含表头(CSV/EXCEL)")
    private Boolean includeHeader = true;

    // ========== 分页配置 ==========
    @Schema(description = "每批导出数量")
    private Integer batchSize = 1000;

    // 辅助方法
    public boolean isExcelFormat() {
        return EXCEL.getId().equalsIgnoreCase(format);
    }

    public boolean isCsvFormat() {
        return CSV.getId().equalsIgnoreCase(format);
    }

    public boolean isJsonFormat() {
        return JSON.getId().equalsIgnoreCase(format);
    }

    public boolean isGzipFormat() {
        return GZIP.getId().equalsIgnoreCase(format) || Boolean.TRUE.equals(compress);
    }

    public String getFileExtension() {
        if (isExcelFormat()) {
            return isGzipFormat() ? EXCEL.getDetailText() : EXCEL.getText();
        } else if (isCsvFormat()) {
            return isGzipFormat() ? CSV.getDetailText() : CSV.getText();
        } else if (isJsonFormat()) {
            return isGzipFormat() ? JSON.getDetailText() : JSON.getText();
        } else {
            return isGzipFormat() ? GZIP.getDetailText() : GZIP.getText();
        }
    }

}
