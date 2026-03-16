package cn.getech.base.demo.dto;

import cn.getech.base.demo.enums.CursorSortByEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Map;
import static cn.getech.base.demo.constant.FieldConstant.CREATE_TIME;

/**
 * 知识库文档搜索返回的通用数据
 */
@Data
@Schema(description = "知识库文档搜索返回的通用数据")
public class KnowledgeDocumentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID", example = "文档ID")
    private Long id;

    @Schema(description = "文档内容", example = "文档内容")
    private String content;

    @Schema(description = "元数据", example = "元数据")
    private Map<String, Object> metadata;

    @Schema(description = "分数", example = "分数")
    private Float score;

    // 创建时间（从metadata中提取并格式化）
    public String getCreatedTime() {
        if (metadata != null && metadata.containsKey(CREATE_TIME)) {
            Object createdTime = metadata.get(CREATE_TIME);
            if (createdTime instanceof String) {
                return (String) createdTime;
            }
        }
        return null;
    }

    // 转换为复合游标
    public CompositeCursorDto toCursor(String sortBy, String sortDirection) {
        CompositeCursorDto cursorDto = new CompositeCursorDto();
        cursorDto.setSecondaryValue(this.id);
        cursorDto.setSortDirection(sortDirection);

        if (CursorSortByEnum.SCORE.getId().equals(sortBy)) {
            cursorDto.setPrimaryValue(String.valueOf(this.score));
        } else if (CursorSortByEnum.CREATE_TIME.getId().equals(sortBy)) {
            cursorDto.setPrimaryValue(this.getCreatedTime());
        }
        return cursorDto;
    }

}
