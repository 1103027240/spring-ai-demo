package cn.getech.base.demo.build;

import cn.getech.base.demo.dto.KnowledgeDocumentDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.KnowledgeDocument;
import cn.getech.base.demo.enums.MessageTaskSyncTypeEnum;
import cn.getech.base.demo.service.ChatMessageService;
import cn.getech.base.demo.utils.ObjectMapperUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
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
     * 标量过滤条件
     */
    public String buildAdvancedFilterExpression(KnowledgeDocumentSearchDto dto) {
        List<String> conditions = new ArrayList<>();

        if (StrUtil.isNotBlank(dto.getTitle()) && StrUtil.isNotBlank(dto.getTitle().trim())) {
            String titleExpr = "metadata[\"title\"] like \"%" + dto.getTitle().trim() + "%\"";
            conditions.add(titleExpr);
        }

        if (StrUtil.isNotBlank(dto.getSummary()) && StrUtil.isNotBlank(dto.getSummary().trim())) {
            String summaryExpr = "metadata[\"summary\"] like \"%" + dto.getSummary().trim() + "%\"";
            conditions.add(summaryExpr);
        }

        if (StrUtil.isNotBlank(dto.getKeyword()) && StrUtil.isNotBlank(dto.getKeyword().trim())) {
            String keywordExpr = "array_contains(metadata[\"keywords\"], \"" + dto.getKeyword().trim() + "\")";
            conditions.add(keywordExpr);
        }

        if (dto.getCategoryId() != null) {
            String categoryIdExpr = "metadata[\"categoryId\"] == " + dto.getCategoryId();
            conditions.add(categoryIdExpr);
        }

        if (dto.getStartTime() != null) {
            String startTimeExpr = "metadata[\"createdTime\"] >= \"" + dto.getStartTime() + "\"";
            conditions.add(startTimeExpr);
        }

        if (dto.getEndTime() != null) {
            String endTimeExpr = "metadata[\"createdTime\"] <= \"" + dto.getEndTime() + "\"";
            conditions.add(endTimeExpr);
        }

        if (CollUtil.isNotEmpty(dto.getTags())) {
            String tagsCondition = dto.getTags().stream()
                    .map(tag -> "array_contains(metadata[\"tags\"], \"" + tag + "\")")
                    .collect(Collectors.joining(" and "));
            conditions.add("(" + tagsCondition + ")");
        }

        return CollUtil.isEmpty(conditions) ? "" : String.join(" and ", conditions);
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
        document.setCreateTime(System.currentTimeMillis());

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

    public boolean buildUpdateKnowledgeDocument(KnowledgeDocument document, KnowledgeDocumentDto dto){
        boolean contentChanged = false;

        if (StrUtil.isNotBlank(dto.getTitle()) && !dto.getTitle().equals(document.getTitle())) {
            document.setTitle(dto.getTitle());
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getContent()) && !dto.getContent().equals(document.getContent())) {
            document.setContent(dto.getContent());
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getSummary()) && !dto.getSummary().equals(document.getSummary())) {
            document.setSummary(dto.getSummary());
            contentChanged = true;
        }

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(document.getCategoryId())) {
            document.setCategoryId(dto.getCategoryId());
            contentChanged = true;
        }

        if (CollUtil.isNotEmpty(dto.getTags())) {
            document.setTags(objectMapperUtils.convertToJson(dto.getTags()));
            contentChanged = true;
        }

        if (CollUtil.isNotEmpty(dto.getKeywords())) {
            document.setKeywords(objectMapperUtils.convertToJson(dto.getKeywords()));
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getSource()) && !dto.getSource().equals(document.getSource())) {
            document.setSource(dto.getSource());
            contentChanged = true;
        }

        if (dto.getPriority() != null && !dto.getPriority().equals(document.getPriority())) {
            document.setPriority(dto.getPriority());
            contentChanged = true;
        }

        if (dto.getStatus() != null && !dto.getStatus().equals(document.getStatus())) {
            document.setStatus(dto.getStatus());
            contentChanged = true;
        }

        document.setIsVectorized(2); // 标记为已修改但未向量化
        document.setVersion(document.getVersion() + 1);

        return contentChanged;
    }

}
