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

    /** ====== 向量条件 ====== **/
    @Schema(description = "文档内容", example = "文档内容")
    private String content;

    @Schema(description = "分数阈值", example = "分数阈值")
    private Float thresholdSimilarity = 0.7f;

    /** ====== 标量条件 ====== **/
    @Schema(description = "标题", example = "标题")
    private String title;

    @Schema(description = "摘要", example = "摘要")
    private String summary;

    @Schema(description = "搜索关键词", example = "退货政策")
    private String keyword;

    @Schema(description = "分类ID", example = "售后政策")
    private String categoryId;

    @Schema(description = "标签列表", example = "[\"退货\", \"退款\"]")
    private List<String> tags;

    @Schema(description = "作者", example = "系统管理员")
    private String author;

    @Schema(description = "文档来源", example = "平台政策")
    private String source;

    @Schema(description = "状态(1:启用,0:禁用,2:待审核,3:已删除)", example = "1")
    private Integer status;

    @Schema(description = "开始时间", example = "开始时间")
    private String startTime;

    @Schema(description = "结束时间", example = "结束时间")
    private String endTime;

    @Min(value = 1, message = "每页记录数不能小于1")
    @Schema(description = "每页记录数，默认20", example = "20")
    private Integer pageSize = 20;

    /**
     * 一级字段按score/createTime排序
     * 二级字段按ID排序，ID一定要有序
     */
    @Schema(description = "排序字段", example = "score/createTime")
    private String sortBy = "score";

    @Schema(description = "排序类型", example = "DESC/ASC")
    private String sortDirection = "DESC";

    /** ====== 游标条件 ====== **/
    @Schema(description = "游标分页标识", example = "eyJkb2N1bWVudElkIjoxMjMsInNpbWlsYXJpdHlTY29yZSI6MC45fQ==")
    private String cursor;

}
