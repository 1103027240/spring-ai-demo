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

    private Double minScore;    // 当前批次的最低分数（用于forward下一页）

    private Double maxScore;    // 当前批次的最高分数（用于backward上一页）

    private Long lastId;        // 最后一个文档的ID（用于混合模式分数相同时的分页）

    private Double lastExactScore;  // 最后一条数据的精确分数（用于精确分页，解决相同分数大量数据的场景）

    private Long lastExactId;       // 最后一条数据的精确ID（用于精确分页，ID是有序的）

    private boolean useHybridMode; // 是否使用混合模式（向量+ID分页）

    private boolean hasLargeSameScoreBlock; // 是否存在大量相同分数块（用于标记特殊场景）
}
