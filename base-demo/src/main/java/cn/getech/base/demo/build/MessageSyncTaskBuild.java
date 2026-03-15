package cn.getech.base.demo.build;

import cn.getech.base.demo.constant.FieldConstant;
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
        task.setSyncType(INCREMENTAL.getId());
        task.setStatus(MessageTaskStatusEnum.PENDING.getId());
        task.setRetryCount(0);
        task.setLastMessageId(determineLastMessageId(userMessage, aiMessage));
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
    private Long determineLastMessageId(ChatMessage userMessage, ChatMessage aiMessage) {
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
                .append(ChatMessageTypeEnum.getText(message.getMessageType()))
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
        metadata.put(FieldConstant.MESSAGE_ID, message.getId());
        metadata.put(FieldConstant.USER_ID, message.getUserId());
        metadata.put(FieldConstant.SESSION_ID, message.getSessionId());
        metadata.put(FieldConstant.MESSAGE_TYPE, message.getMessageType());
        metadata.put(FieldConstant.IS_AI, message.getIsAiResponse());
        metadata.put(FieldConstant.WORKFLOW_EXECUTION_ID, message.getWorkflowExecutionId());
        metadata.put(FieldConstant.CREATE_TIME, message.getCreateTime());
        metadata.put(FieldConstant.SYNC_TIME, System.currentTimeMillis());
        metadata.put(FieldConstant.SOURCE, SOURCE_MYSQL_SYNC);

        if (StrUtil.isNotBlank(message.getIntent())) {
            metadata.put(FieldConstant.INTENT, message.getIntent());
        }

        if (StrUtil.isNotBlank(message.getSentiment())) {
            metadata.put(FieldConstant.SENTIMENT, message.getSentiment());
        }

        if (message.getResponseTime() != null) {
            metadata.put(FieldConstant.RESPONSE_TIME_MS, message.getResponseTime());
        }

        return metadata;
    }

}
