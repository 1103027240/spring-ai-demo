package cn.example.base.demo.param.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "知识库文档请求参数")
public class KnowledgeDocumentDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID", example = "1")
    private Long id;

    @NotBlank(message = "文档标题不能为空")
    @Schema(description = "文档标题", required = true, example = "七天无理由退货政策")
    private String title;

    @NotBlank(message = "文档内容不能为空")
    @Schema(description = "文档内容", required = true, example = "## 七天无理由退货政策\n\n### 一、适用范围...")
    private String content;

    @Schema(description = "文档摘要", example = "7天无理由退货政策详细说明")
    private String summary;

    @Schema(description = "分类ID", example = "售后政策")
    private Long categoryId;

    @Schema(description = "标签列表", example = "[\"退货\", \"退款\", \"七天无理由\"]")
    private List<String> tags;

    @Schema(description = "关键词列表", example = "[\"退货政策\", \"七天\", \"无理由\"]")
    private List<String> keywords;

    @Schema(description = "文档来源", example = "平台政策")
    private String source;

    @Schema(description = "作者", example = "系统管理员")
    private String author;

    @Schema(description = "优先级（0-100）", example = "100")
    private Integer priority = 0;

    @Schema(description = "状态：1-启用，0-禁用，2-待审核，3-已删除", example = "1")
    private Integer status = 1;

}