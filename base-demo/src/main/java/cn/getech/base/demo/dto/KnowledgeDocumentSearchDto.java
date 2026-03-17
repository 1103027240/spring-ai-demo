package cn.getech.base.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Data
@Slf4j
@Schema(description = "知识库文档搜索请求参数")
public class KnowledgeDocumentSearchDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ====== 向量搜索参数 ====== **/

    @Schema(description = "文档内容", example = "文档内容")
    private String content;

    @Schema(description = "相似度分数阈值", example = "相似度分数阈值")
    private Float thresholdSimilarity = 0.7f;

    @Min(value = 1, message = "topK至少为1")
    @Max(value = 1000, message = "topK最大为1000")
    @Schema(description = "向量搜索返回数量", example = "向量搜索返回数量")
    private Integer topK = 100;

    /** ====== 标量搜素参数 ====== **/

    @Schema(description = "标题", example = "标题")
    private String title;

    @Schema(description = "摘要", example = "摘要")
    private String summary;

    @Schema(description = "搜索关键词", example = "退货政策")
    private String keyword;

    @Schema(description = "分类ID", example = "售后政策")
    private Long categoryId;

    @Schema(description = "标签列表", example = "[\"退货\", \"退款\"]")
    private List<String> tags;

    @Schema(description = "作者", example = "系统管理员")
    private String author;

    @Schema(description = "文档来源", example = "平台政策")
    private String source;

    @Schema(description = "状态", example = "1:启用,0:禁用,2:待审核,3:已删除")
    private Integer status;

    @Schema(description = "开始时间", example = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间", example = "结束时间")
    private Long endTime;

    // ========== 排序参数 ==========

    @NotNull(message = "主排序字段不能为空")
    @Schema(description = "主排序字段: score（分数）, createTime（创建时间）, docId", example = "主排序字段: score（分数）, createTime（创建时间）, docId)")
    private String sortField = "score";

    @NotNull(message = "主排序方向不能为空")
    @Schema(description = "主排序方向: asc(升序), desc(降序)", example = "主排序方向: asc(升序), desc(降序)")
    private String sortDirection = "desc";

    @Schema(description = "次排序字段 (主排序相同时的备用排序)", example = "次排序字段 (主排序相同时的备用排序)")
    private String secondSortField = "docId";

    @Schema(description = "次排序方向", example = "次排序方向")
    private String secondSortDirection = "desc";

    // ========== 分页参数 ==========

    @Min(value = 1, message = "每页记录数不能小于1")
    @Schema(description = "每页记录数，默认20", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "游标方向", example = "next-下一页, prev-上一页, first-首页")
    private String cursorDirection = "first";

    @Schema(description = "向前游标值 (用于上一页)", example = "向前游标值 (用于上一页)")
    private String forwardCursor;

    @Schema(description = "向后游标值 (用于下一页)", example = "向后游标值 (用于下一页)")
    private String backwardCursor;

    // 辅助方法
    public boolean isFirstPage() {
        return "first".equals(cursorDirection) || (forwardCursor == null && backwardCursor == null);
    }

    public boolean isNextPage() {
        return "next".equals(cursorDirection) && backwardCursor != null;
    }

    public boolean isPrevPage() {
        return "prev".equals(cursorDirection) && forwardCursor != null;
    }

    /**
     * 构建游标值 (使用Base64编码避免特殊字符冲突)
     */
    public String encodeCursor(String primaryValue, String secondaryValue) {
        if (primaryValue == null) primaryValue = "0";
        if (secondaryValue == null) secondaryValue = "0";
        String combined = primaryValue + "|" + secondaryValue;
        return Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析游标值
     * @return [primaryValue, secondaryValue]
     */
    public String[] decodeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return new String[]{"0", "0"};
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length == 2) {
                return parts;
            }
            log.warn("游标格式错误: {}", cursor);
            return new String[]{"0", "0"};
        } catch (Exception e) {
            log.error("游标解码失败: {}", cursor, e);
            return new String[]{"0", "0"};
        }
    }

}
