package cn.example.ai.demo.build;

import cn.example.ai.demo.check.KnowledgeDocumentCheck;
import cn.example.ai.demo.param.dto.KnowledgeDocumentSearchDto;
import cn.example.ai.demo.param.vo.KnowledgeDocumentVO;
import cn.example.ai.demo.enums.SortDirectionEnum;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;
import static cn.example.ai.demo.constant.FieldConstant.*;

@Slf4j
@Component
public class KnowledgeDocumentSearchBuild {

    @Value("${customer.milvus.search.nprobe:128}")
    private Integer nprobe;

    @Autowired
    private KnowledgeDocumentCheck knowledgeDocumentCheck;

    /**
     * 构建标量过滤表达式（向量+标量混合查询）
     */
    public String buildAdvancedFilterExpression(KnowledgeDocumentSearchDto dto) {
        List<String> conditions = new ArrayList<>();

        addLikeCondition(conditions, "title", dto.getTitle());
        addLikeCondition(conditions, "summary", dto.getSummary());
        addLikeCondition(conditions, "author", dto.getAuthor());
        addLikeCondition(conditions, "source", dto.getSource());
        addArrayContainsCondition(conditions, "keywords", dto.getKeyword());
        addEqualCondition(conditions, "categoryId", dto.getCategoryId());
        addEqualCondition(conditions, "status", dto.getStatus());
        addTimeRangeCondition(conditions, dto.getStartTime(), dto.getEndTime());
        addTagsCondition(conditions, dto.getTags());

        return CollUtil.isEmpty(conditions) ? "" : String.join(" and ", conditions);
    }

    private void addLikeCondition(List<String> conditions, String field, String value) {
        if (StrUtil.isNotBlank(value)) {
            String trimmedValue = value.trim();
            if (StrUtil.isNotBlank(trimmedValue)) {
                conditions.add("metadata[\"" + field + "\"] like \"%" + trimmedValue + "%\"");
            }
        }
    }

    private void addArrayContainsCondition(List<String> conditions, String field, String value) {
        if (StrUtil.isNotBlank(value)) {
            String trimmedValue = value.trim();
            if (StrUtil.isNotBlank(trimmedValue)) {
                conditions.add("array_contains(metadata[\"" + field + "\"], \"" + trimmedValue + "\")");
            }
        }
    }

    private void addTimeRangeCondition(List<String> conditions, Long startTime, Long endTime) {
        if (startTime != null) {
            conditions.add("metadata[\"createTime\"] >= " + startTime);
        }
        if (endTime != null) {
            conditions.add("metadata[\"createTime\"] <= " + endTime);
        }
    }

    private void addEqualCondition(List<String> conditions, String field, Object value) {
        if (value != null) {
            conditions.add("metadata[\"" + field + "\"] == " + value);
        }
    }

    private void addTagsCondition(List<String> conditions, List<String> tags) {
        if (CollUtil.isNotEmpty(tags)) {
            String tagsCondition = tags.stream()
                    .map(tag -> "array_contains(metadata[\"tags\"], \"" + tag + "\")")
                    .collect(Collectors.joining(" and "));
            conditions.add("(" + tagsCondition + ")");
        }
    }

    /**
     * 查找游标位置，找不到精确匹配时返回最接近的位置（不降级成线性查询）
     */
    public int findCursorIndex(List<KnowledgeDocumentVO> sortedResults, String primaryCursor, String secondCursor, KnowledgeDocumentSearchDto dto) {
        if (CollUtil.isEmpty(sortedResults)) {
            return -1;
        }

        int low = 0;
        int high = sortedResults.size() - 1;
        boolean isDesc = SortDirectionEnum.DESC.getId().equalsIgnoreCase(dto.getSortDirection());

        while (low <= high) {
            int mid = (low + high) >>> 1;
            KnowledgeDocumentVO doc = sortedResults.get(mid);
            String docPrimarySortKey = doc.getSortKey(dto.getSortField());
            String docSecondSortKey = doc.getSortKey(dto.getSecondSortField());

            int primaryCompare = knowledgeDocumentCheck.compareSortKey(docPrimarySortKey, primaryCursor, dto.getSortField());
            if (primaryCompare == 0) {
                int secondCompare = knowledgeDocumentCheck.compareSortKey(docSecondSortKey, secondCursor, dto.getSecondSortField());
                if (secondCompare == 0) {
                    return mid;
                }

                if (isDesc) {
                    if (secondCompare > 0) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                } else {
                    if (secondCompare < 0) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                }
            } else {
                if (isDesc) {
                    if (primaryCompare > 0) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                } else {
                    if (primaryCompare < 0) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                }
            }
        }

        // 根据排序方向返回合适的游标位置
        // 降序：返回第一个大于目标值的位置（high）
        // 升序：返回第一个大于目标值的位置（low）
        return SortDirectionEnum.DESC.getId().equalsIgnoreCase(dto.getSortDirection()) ? high : low;
    }

    /**
     * 构建游标
     */
    public String buildCursor(KnowledgeDocumentSearchDto dto, KnowledgeDocumentVO doc, int currentPage) {
        return dto.encodeCursor(
                currentPage,
                doc.getSortKey(dto.getSortField()),
                doc.getSortKey(dto.getSecondSortField()));
    }

    public List<KnowledgeDocumentVO> convertSearchResults(SearchResp searchResp) {
        return searchResp.getSearchResults().get(0).stream()
                .map(hit -> {
                    KnowledgeDocumentVO doc = new KnowledgeDocumentVO();
                    doc.setDocId(Long.parseLong((String) hit.getId()));
                    doc.setScore(hit.getScore());

                    Map<String, Object> entityData = hit.getEntity();
                    if (entityData != null) {
                        doc.setContent((String) entityData.get(CONTENT));

                        Object metadataObj = entityData.get(METADATA);
                        if (metadataObj != null) {
                            Map<String, Object> metadata = JSONUtil.toBean(metadataObj.toString(), Map.class);
                            doc.setMetadata(metadata);

                            Object createTimeObj = metadata.get(CREATE_TIME);
                            if (createTimeObj != null) {
                                doc.setCreateTime((Long) createTimeObj);
                            }
                        }
                    }
                    return doc;
                }).collect(Collectors.toList());
    }

    /**
     * 计算动态 topK 值（支持无限分页）
     * 计算逻辑：目标索引 + 每页记录数 * 3（预留空间）
     * 限制：Milvus不能超过16384
     */
    public int calculateDynamicTopK(KnowledgeDocumentSearchDto dto) {
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 20;
        int baseQuerySize = dto.getTargetDataIndex();

        int extraBuffer = pageSize * 3;
        int calculatedTopK = baseQuerySize + extraBuffer;

        // 只有用户显式设置topK时才考虑，否则使用动态计算的值
        int finalTopK = (dto.getTopK() != null) ? Math.max(dto.getTopK(), calculatedTopK) : calculatedTopK;
        return Math.min(finalTopK, 16384);
    }

    /**
     * 构建搜索参数
     */
    public Map<String, Object> buildSearchParams(KnowledgeDocumentSearchDto dto) {
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("nprobe", nprobe);
        searchParams.put("metric_type", "COSINE");
        searchParams.put("radius", dto.getThresholdSimilarity());  // 最小相似度阈值
        searchParams.put("range_filter", 1.0);  // 最大相似度上限
        return searchParams;
    }

}
