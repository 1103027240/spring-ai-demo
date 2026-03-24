package cn.example.base.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocumentExportVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "导出任务ID")
    private String taskId;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "导出状态")
    private String status; // PROCESSING, COMPLETED, FAILED, CANCELLED

    @Schema(description = "耗时(毫秒)")
    private Long duration;

    @Schema(description = "导出格式")
    private String format;

    @Schema(description = "压缩比(%)")
    private Double compressionRatio;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "下载URL")
    private String downloadUrl;

}
