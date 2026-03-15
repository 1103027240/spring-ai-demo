package cn.getech.base.demo.dto;

import cn.getech.base.demo.entity.KnowledgeDocument;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 游标搜索结果（包含文档列表和分数范围）
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CursorSearchResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<KnowledgeDocument> documents;

    private Double minScore;    // 最低分数（用于forward）

    private Double maxScore;    // 最高分数（用于backward）

    private Long lastId;        // 最后一个文档的ID（用于混合模式分数相同时的分页）

    private boolean useHybridMode; // 是否使用混合模式（向量+ID分页）

}
