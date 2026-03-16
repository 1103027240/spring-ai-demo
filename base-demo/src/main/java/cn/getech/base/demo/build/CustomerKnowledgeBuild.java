package cn.getech.base.demo.build;

import cn.getech.base.demo.dto.CompositeCursorDto;
import cn.getech.base.demo.dto.KnowledgeDocumentDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.dto.KnowledgeDocumentVO;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.KnowledgeDocument;
import cn.getech.base.demo.enums.CursorSortByEnum;
import cn.getech.base.demo.enums.CursorSortDirectionEnum;
import cn.getech.base.demo.enums.MessageTaskSyncTypeEnum;
import cn.getech.base.demo.service.ChatMessageService;
import cn.getech.base.demo.utils.ObjectMapperUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonObject;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.enums.MessageTaskSyncTypeEnum.INCREMENTAL;

@Slf4j
@Component
public class CustomerKnowledgeBuild {

    @Resource(name = "chatMessageVectorStore")
    private VectorStore chatMessageVectorStore;

    @Autowired
    private MessageSyncTaskBuild messageSyncTaskBuild;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ObjectMapperUtils objectMapperUtils;

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
     * 转换高级搜索结果
     */
    public List<KnowledgeDocumentVO> convertAdvancedSearchResults(SearchResultsWrapper wrapper) {
        List<KnowledgeDocumentVO> documents = new ArrayList<>();

        for (int i = 0; i < wrapper.getRowRecords().size(); i++) {
            KnowledgeDocumentVO doc = new KnowledgeDocumentVO();

            Map<String, Object> record = wrapper.getRowRecords().get(i).getFieldValues();
            doc.setId(Long.parseLong(record.get(DOC_ID).toString()));
            doc.setContent((String) record.get(CONTENT));
            doc.setScore((Float) record.get(SCORE));

            if (record.get(METADATA) != null) {
                JsonObject metadataJson = (JsonObject) record.get(METADATA);
                Map<String, Object> metadataMap = JSONUtil.toBean(metadataJson.toString(), Map.class);
                doc.setMetadata(metadataMap);
            }

            documents.add(doc);
        }

        return documents;
    }

    /**
     * 标量过滤条件
     */
    public String buildAdvancedFilterExpression(KnowledgeDocumentSearchDto dto) {
        List<String> conditions = new ArrayList<>();

        if (StrUtil.isNotBlank(dto.getTitle()) && StrUtil.isNotBlank(dto.getTitle().trim())) {
            String title = dto.getTitle().trim();
            conditions.add(String.format("(metadata['title'] like '%%%s%%')", title));
        }

        if (StrUtil.isNotBlank(dto.getSummary()) && StrUtil.isNotBlank(dto.getSummary().trim())) {
            String summary = dto.getSummary().trim();
            conditions.add(String.format("(metadata['summary'] like '%%%s%%')", summary));
        }

        if (StrUtil.isNotBlank(dto.getKeyword()) && StrUtil.isNotBlank(dto.getKeyword().trim())) {
            String keyword = dto.getKeyword().trim();
            conditions.add(String.format("json_contains(metadata['keywords'], '%s')", keyword, keyword));
        }

        if (dto.getCategoryId() != null) {
            conditions.add(String.format("metadata['categoryId'] = '%s'", dto.getCategoryId()));
        }

        if (StrUtil.isNotBlank(dto.getStartTime()) && StrUtil.isNotBlank(dto.getStartTime().trim())) {
            conditions.add(String.format("metadata['created_at'] >= '%s'", dto.getStartTime()));
        }

        if (StrUtil.isNotBlank(dto.getEndTime()) && StrUtil.isNotBlank(dto.getEndTime().trim())) {
            conditions.add(String.format("metadata['created_at'] <= '%s'", dto.getEndTime()));
        }

        if (CollUtil.isNotEmpty(dto.getTags())) {
            String tagsCondition = dto.getTags().stream()
                    .map(tag -> String.format("array_contains(metadata['tags'], '%s')", tag))
                    .collect(Collectors.joining(" and "));
            conditions.add("(" + tagsCondition + ")");
        }

        if (dto.getThresholdSimilarity() != null && dto.getThresholdSimilarity() > 0) {
            conditions.add(String.format("score >= %f", dto.getThresholdSimilarity()));
        }

        return CollUtil.isEmpty(conditions) ? "" : String.join(" and ", conditions);
    }

    /**
     * 游标过滤条件
     */
    public String buildCursorFilter(KnowledgeDocumentSearchDto dto) {
        CompositeCursorDto cursorDto = CompositeCursorDto.decodeCursor(dto.getCursor());
        if (cursorDto == null) {
            return "";
        }
        return buildCursorFilterForCheck(cursorDto, dto);
    }

    /**
     * 构建检查用的游标过滤条件
     */
    public String buildCursorFilterForCheck(CompositeCursorDto cursorDto, KnowledgeDocumentSearchDto dto) {
        String primaryField;
        if (CursorSortByEnum.SCORE.getId().equals(dto.getSortBy())) {
            primaryField = CursorSortByEnum.SCORE.getId();
        } else if (CursorSortByEnum.CREATE_TIME.getId().equals(dto.getSortBy())) {
            primaryField = "metadata['createTime']";
        } else {
            primaryField = CursorSortByEnum.SCORE.getId();
        }

        // 构建复合游标过滤条件
        String operator = CursorSortDirectionEnum.DESC.getId().equals(cursorDto.getSortDirection())
                ? CursorSortDirectionEnum.DESC.getText() : CursorSortDirectionEnum.ASC.getText();

        // 例如按score降序：score < cursor.getPrimaryValue() or (score = cursor.getPrimaryValue() and id < cursor.getSecondaryValue())
        return String.format("(%s %s '%s' or (%s = '%s' and id %s %d))",
                primaryField, operator, cursorDto.getPrimaryValue(),
                primaryField, cursorDto.getPrimaryValue(),
                operator, cursorDto.getSecondaryValue()
        );
    }

    /**
     * 合并过滤条件
     */
    public String mergeFilters(String... filters) {
        List<String> validFilters = Arrays.stream(filters)
                .filter(f -> StrUtil.isNotBlank(f) && StrUtil.isNotBlank(f.trim()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validFilters)) {
            return "";
        }
        return String.join(" and ", validFilters);
    }

    /**
     * 构建排序表达式
     */
    public String buildOrderByExpression(KnowledgeDocumentSearchDto dto) {
        if (CursorSortByEnum.SCORE.getId().equals(dto.getSortBy())) {
            return String.format("score %s, id %s", dto.getSortDirection(), dto.getSortDirection());
        } else if (CursorSortByEnum.CREATE_TIME.getId().equals(dto.getSortBy())) {
            return String.format("metadata['createTime'] %s, id %s", dto.getSortDirection(), dto.getSortDirection());
        }
        return "score DESC, id DESC";
    }

    public KnowledgeDocument buildAddKnowledgeDocument(KnowledgeDocumentDto dto){
        KnowledgeDocument document = new KnowledgeDocument();
        document.setTitle(dto.getTitle());
        document.setContent(dto.getContent());
        document.setSummary(dto.getSummary());
        document.setCategoryId(dto.getCategoryId());
        document.setSource(dto.getSource());
        document.setAuthor(dto.getAuthor());
        document.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        document.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        document.setIsVectorized(0);
        document.setCreateTime(LocalDateTime.now());

        if (CollUtil.isNotEmpty(dto.getTags())) {
            document.setTags(objectMapperUtils.convertToJson(dto.getTags()));
        }

        if (CollUtil.isNotEmpty(dto.getKeywords())) {
            document.setKeywords(objectMapperUtils.convertToJson(dto.getKeywords()));
        }

        if (CollUtil.isNotEmpty(dto.getMetadata())) {
            document.setMetadata(objectMapperUtils.convertToJson(dto.getMetadata()));
        }

        return document;
    }

}
