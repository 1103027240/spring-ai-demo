package cn.example.base.demo.dto;

import cn.example.base.demo.enums.CursorSortByEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 知识库文档搜索返回的通用数据
 */
@Data
@Schema(description = "知识库文档搜索返回的通用数据")
public class KnowledgeDocumentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID", example = "文档ID")
    private Long docId;

    @Schema(description = "文档内容", example = "文档内容")
    private String content;

    @Schema(description = "元数据", example = "元数据")
    private Map<String, Object> metadata;

    @Schema(description = "分数", example = "分数")
    private Float score;

    @Schema(description = "创建时间", example = "创建时间")
    private Long createTime;

    // 获取排序键值（用于游标分页）
    public String getSortKey(String sortField) {
        CursorSortByEnum cursorSortEnum = CursorSortByEnum.getCursorSort(sortField);
        switch (cursorSortEnum) {
            case CREATE_TIME:
                return String.valueOf(createTime);
            case SCORE:
                return String.format("%.6f", score != null ? score : 0.0f);
            case DOC_ID:
            default:
                return String.valueOf(docId);
        }
    }

}
