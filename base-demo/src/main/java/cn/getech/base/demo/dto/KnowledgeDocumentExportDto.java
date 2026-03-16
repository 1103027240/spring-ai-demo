package cn.getech.base.demo.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeDocumentExportDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 导出格式：json、csv、xml
     */
    private String format = "json";

    /**
     * 分类筛选
     */
    private String category;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 关键词筛选
     */
    private String keyword;

    /**
     * 状态筛选
     */
    private Integer status;

    /**
     * 作者筛选
     */
    private String author;

    /**
     * 是否包含已删除的文档
     */
    private Boolean includeDeleted = false;

    /**
     * 字段列表（逗号分隔），用于导出特定字段
     */
    private String fields = "id,title,category,author,create_time";

    /**
     * 排序字段
     */
    private String orderBy = "create_time";

    /**
     * 排序方向：asc/desc
     */
    private String orderDirection = "desc";

    /**
     * 导出文件编码
     */
    private String encoding = "UTF-8";

    /**
     * 分页导出，从第几页开始
     */
    private Integer page = 1;

    /**
     * 分页导出，每页数量
     */
    private Integer size = 1000;

}
