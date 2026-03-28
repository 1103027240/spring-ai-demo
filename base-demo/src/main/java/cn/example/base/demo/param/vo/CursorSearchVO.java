package cn.example.base.demo.param.vo;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * 游标搜索返回的通用数据
 */
@Data
@Builder
@Setter
@Getter
@Schema(description = "游标搜索返回的通用数据")
public class CursorSearchVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表", example = "数据列表")
    private List<T> data;

    @Schema(description = "下一页游标", example = "下一页游标")
    private String nextCursor;

    @Schema(description = "上一页游标", example = "上一页游标")
    private String prevCursor;

    @Schema(description = "是否有下一页数据", example = "是否有下一页数据")
    private Boolean hasNext;

    @Schema(description = "是否有上一页数据", example = "是否有上一页数据")
    private Boolean hasPrev;

    @Schema(description = "每页记录数", example = "每页记录数")
    private Integer pageSize;

    @Schema(description = "当前页记录数", example = "当前页记录数")
    private Integer currentSize;

    @Schema(description = "总记录数", example = "总记录数")
    private Long totalSize;

    @Schema(description = "排序字段", example = "score/createTime")
    private String sortBy;

    @Schema(description = "排序类型", example = "DESC/ASC")
    private String sortDirection;

    @Schema(description = "排序信息", example = "排序信息")
    private SortInfoVO sortInfoVO;

    /**
     * 构建空结果
     */
    public static <T> CursorSearchVO<T> empty() {
        return CursorSearchVO.<T>builder()
                .data(List.of())
                .hasNext(false)
                .hasPrev(false)
                .pageSize(0)
                .currentSize(0)
                .build();
    }

    /**
     * 构建成功结果
     */
    public static <T> CursorSearchVO<T> success(
            List<T> data,
            String nextCursor,
            String prevCursor,
            boolean hasNext,
            boolean hasPrev,
            Integer pageSize) {

        return CursorSearchVO.<T>builder()
                .data(data)
                .nextCursor(nextCursor)
                .prevCursor(prevCursor)
                .hasNext(hasNext)
                .hasPrev(hasPrev)
                .pageSize(pageSize)
                .currentSize(CollUtil.isNotEmpty(data) ? data.size() : 0)
                .build();
    }

}
