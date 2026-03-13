package cn.getech.base.demo.build;

import cn.getech.base.demo.constant.FieldValueConstant;
import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.entity.MessageSyncTask;
import cn.getech.base.demo.enums.ChatMessageTypeEnum;
import cn.getech.base.demo.enums.MessageTaskStatusEnum;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cn.getech.base.demo.constant.FieldValueConstant.*;
import static cn.getech.base.demo.enums.MessageTaskSyncTypeEnum.INCREMENTAL;

@Slf4j
@Component
public class MessageSyncTaskBuild {

    /**
     * 创建同步任务
     */
    public MessageSyncTask buildSyncTask(CustomerServiceStateDto state, ChatMessage userMessage, ChatMessage aiMessage) {
        MessageSyncTask task = new MessageSyncTask();
        task.setSessionId(state.getSessionId());
        task.setSyncType(INCREMENTAL.getCode());
        task.setStatus(MessageTaskStatusEnum.PENDING.getCode());
        task.setRetryCount(0);
        task.setLastMessageId(determineLastMessageId(aiMessage, userMessage));
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        return task;
    }

    /**
     * 构建文档列表
     */
    public List<Document> buildDocument(List<ChatMessage> messages) {
        List<Document> documents = new ArrayList<>();
        for (ChatMessage message : messages) {
            try {
                documents.add(buildMilvusDocument(message));
            } catch (Exception e) {
                log.error("【异步同步】构建Milvus文档失败，消息ID: {}", message.getId(), e);
            }
        }
        return documents;
    }

    /**
     * 确定最后的消息ID
     */
    private Long determineLastMessageId(ChatMessage aiMessage, ChatMessage userMessage) {
        if (aiMessage != null) {
            return aiMessage.getId();
        }
        return userMessage != null ? userMessage.getId() : null;
    }

    /**
     * 构建Milvus文档
     */
    private Document buildMilvusDocument(ChatMessage message) {
        Document document = new Document(buildDocumentContent(message));
        document.getMetadata().putAll(buildDocumentMetadata(message));
        return document;
    }

    /**
     * 构建文档内容
     */
    private String buildDocumentContent(ChatMessage message) {
        StringBuilder content = new StringBuilder();
        content.append(CUSTOMER_SERVICE_MESSAGE_TYPE)
                .append(ChatMessageTypeEnum.getDescription(message.getMessageType()))
                .append(CUSTOMER_SERVICE_CONTENT)
                .append(message.getContent());

        if (StrUtil.isNotBlank(message.getIntent())) {
            content.append(CUSTOMER_SERVICE_INTENT).append(message.getIntent());
        }

        if (StrUtil.isNotBlank(message.getSentiment())) {
            content.append(CUSTOMER_SERVICE_SENTIMENT).append(message.getSentiment());
        }

        return content.toString();
    }

    /**
     * 构建文档元数据
     */
    private Map<String, Object> buildDocumentMetadata(ChatMessage message) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(FieldValueConstant.MESSAGE_ID, message.getId());
        metadata.put(FieldValueConstant.USER_ID, message.getUserId());
        metadata.put(FieldValueConstant.SESSION_ID, message.getSessionId());
        metadata.put(FieldValueConstant.MESSAGE_TYPE, message.getMessageType());
        metadata.put(FieldValueConstant.IS_AI, message.getIsAiResponse());
        metadata.put(FieldValueConstant.WORKFLOW_EXECUTION_ID, message.getWorkflowExecutionId());
        metadata.put(FieldValueConstant.CREATE_TIME, message.getCreateTime());
        metadata.put(FieldValueConstant.SYNC_TIME, System.currentTimeMillis());
        metadata.put(FieldValueConstant.SOURCE, SOURCE_MYSQL_SYNC);

        if (StrUtil.isNotBlank(message.getIntent())) {
            metadata.put(FieldValueConstant.INTENT, message.getIntent());
        }

        if (StrUtil.isNotBlank(message.getSentiment())) {
            metadata.put(FieldValueConstant.SENTIMENT, message.getSentiment());
        }

        if (message.getResponseTime() != null) {
            metadata.put(FieldValueConstant.RESPONSE_TIME_MS, message.getResponseTime());
        }

        return metadata;
    }

}
