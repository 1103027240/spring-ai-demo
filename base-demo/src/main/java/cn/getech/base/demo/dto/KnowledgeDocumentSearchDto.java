package cn.getech.base.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "知识库文档搜索请求参数")
public class KnowledgeDocumentSearchDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词", example = "退货政策")
    private String keyword;

    @Schema(description = "分类名称", example = "售后政策")
    private String category;

    @Schema(description = "标签列表", example = "[\"退货\", \"退款\"]")
    private List<String> tags;

    @Schema(description = "作者", example = "系统管理员")
    private String author;

    @Schema(description = "状态(1:启用,0:禁用,2:待审核,3:已删除)", example = "1")
    private Integer status;

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "当前页码，默认1", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页记录数不能小于1")
    @Schema(description = "每页记录数，默认20", example = "20")
    private Integer size = 20;

    @Schema(description = "是否按优先级排序", example = "true")
    private Boolean orderByPriority = false;

    @Schema(description = "是否按查看次数排序", example = "false")
    private Boolean orderByViewCount = false;

    @Schema(description = "搜索模式：keyword(关键词搜索), vector(向量搜索), hybrid(混合搜索)", example = "hybrid", allowableValues = {"keyword", "vector", "hybrid"})
    private String searchMode = "hybrid";

    @Schema(description = "游标分页标识", example = "eyJkb2N1bWVudElkIjoxMjMsInNpbWlsYXJpdHlTY29yZSI6MC45fQ==")
    private String cursor;

    @Schema(description = "分页路径ID（用于Redis存储分页历史，支持无限分页）", example = "abc123")
    private String pathId;

    @Schema(description = "分页方向：forward(下一页), backward(上一页), first(首页)", example = "forward", allowableValues = {"forward", "backward", "first"})
    private String cursorDirection = "forward";

    @Schema(description = "是否启用混合模式（分数连续性分析），默认false。如果文档ID有序，建议设为false以提高性能", example = "false")
    private Boolean enableHybridMode = false;

}
