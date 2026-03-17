package cn.getech.base.demo.build;

import cn.getech.base.demo.check.CustomerKnowledgeCheck;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.enums.MessageTaskSyncTypeEnum;
import cn.getech.base.demo.enums.SortDirectionEnum;
import cn.getech.base.demo.service.ChatMessageService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;
import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.enums.MessageTaskSyncTypeEnum.INCREMENTAL;

@Slf4j
@Component
public class CustomerKnowledgeBuild {

    @Value("${customer.milvus.search.nprobe:128}")
    private Integer nprobe;

    @Resource(name = "chatMessageVectorStore")
    private VectorStore chatMessageVectorStore;

    @Autowired
    private MessageSyncTaskBuild messageSyncTaskBuild;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private CustomerKnowledgeCheck customerKnowledgeCheck;

    /**
     * 同步消息到Milvus
     */
    public boolean syncMessagesToMilvus(String sessionId, Integer syncType) {
        try {
            List<ChatMessage> chatMessages = selectMessagesBySyncType(sessionId, syncType);
            log.info("【异步同步】同步类型: {}, sessionId: {}, 消息数量: {}", MessageTaskSyncTypeEnum.getText(syncType), sessionId, chatMessages.size());

            if (CollUtil.isEmpty(chatMessages)) {
                log.info("【异步同步】没有需要同步的消息，sessionId: {}", sessionId);
                return true;
            }

            List<Document> documents = messageSyncTaskBuild.buildDocument(chatMessages);
            if (CollUtil.isEmpty(documents)) {
                log.warn("【异步同步】没有有效的文档需要同步，sessionId: {}", sessionId);
                return true;
            }
            chatMessageVectorStore.add(documents);
            return true;
        } catch (Exception e) {
            log.error("【异步同步】同步消息到Milvus失败，sessionId: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 根据同步类型选择消息
     */
    public List<ChatMessage> selectMessagesBySyncType(String sessionId, Integer syncType) {
        if (INCREMENTAL.getId().equals(syncType)) {
            return chatMessageService.selectUnsyncedMessages(sessionId);
        }
        return chatMessageService.selectAllValidMessages(sessionId);
    }

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
     * 查找游标位置（二分查找，没有找到时，不降级成线性查询），找不到精确匹配时返回最接近的位置
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

            int primaryCompare = customerKnowledgeCheck.compareSortKey(docPrimarySortKey, primaryCursor, dto.getSortField());

            if (primaryCompare == 0) {
                int secondCompare = customerKnowledgeCheck.compareSortKey(docSecondSortKey, secondCursor, dto.getSecondSortField());
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

        return low;
    }

    /**
     * 构建游标
     */
    public String buildCursor(KnowledgeDocumentSearchDto dto, KnowledgeDocumentVO doc, int pageNum) {
        return dto.encodeCursor(
                pageNum,
                doc.getSortKey(dto.getSortField()),
                doc.getSortKey(dto.getSecondSortField())
        );
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
     * 计算逻辑：(页码 + 1) * 页面大小 + 页面大小*3（预留空间）
     */
    public int calculateDynamicTopK(KnowledgeDocumentSearchDto dto) {
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 20;
        int currentPageNum = dto.getCurrentPageNum();

        int baseQuerySize = (currentPageNum + 1) * pageSize;
        int extraBuffer = pageSize * 3;

        return baseQuerySize + extraBuffer;
    }

    /**
     * 构建搜索参数
     */
    public Map<String, Object> buildSearchParams(KnowledgeDocumentSearchDto dto) {
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("nprobe", nprobe);
        searchParams.put("metric_type", "COSINE");
        searchParams.put("radius", 1.0 - dto.getThresholdSimilarity());
        searchParams.put("range_filter", 1.0);
        return searchParams;
    }

}
