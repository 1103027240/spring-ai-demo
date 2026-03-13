package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.ChatMessageBuild;
import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatMessage;
import cn.getech.base.demo.mapper.ChatMessageMapper;
import cn.getech.base.demo.service.ChatMessageService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Value("${sync.mysql.retention.count:20}")
    private String mysqlRetentionCount;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatMessageBuild chatMessageBuild;

    /**
     * 批量保存用户消息和AI消息
     */
    @Override
    public List<ChatMessage> batchSaveMessages(CustomerServiceStateDto state) {
        List<ChatMessage> messages = new ArrayList<>();

        // 保存用户消息
        messages.add(chatMessageBuild.buildUserChatMessage(state));

        // 保存AI回复消息
        if (StrUtil.isNotBlank(state.getAiResponse())) {
            messages.add(chatMessageBuild.buildAiChatMessage(state));
        }

        // 批量插入
        if (CollUtil.isNotEmpty(messages)) {
            chatMessageMapper.batchInsert(messages);
        }

        return messages;
    }

    /**
     * 清理Mysql中的会话消息
     */
    @Override
    public void cleanupOldMessageInMysql(Long userId) {
        int mysqlRetentionMaxCount = Integer.parseInt(mysqlRetentionCount);
        int messageCount = chatMessageMapper.countByUserId(userId);
        int messagesDelete = messageCount - mysqlRetentionMaxCount;
        if (messagesDelete > 0) {
            int deletedCount = chatMessageMapper.deleteOldMessages(userId, messagesDelete);
            log.info("【清理旧消息】userId: {}, 删除了 {} 条消息", userId, deletedCount);
        }
    }

    @Override
    public void updateMessageSyncStatus(String sessionId) {
        try {
            int updatedCount = chatMessageMapper.updateSyncStatusToSynced(sessionId);
            log.info("【异步同步】更新消息同步状态为已完成，sessionId: {}, 更新数量: {}", sessionId, updatedCount);
        } catch (Exception e) {
            log.error("【异步同步】更新消息同步状态失败", e);
        }
    }

    @Override
    public List<ChatMessage> selectUnsyncedMessages(String sessionId) {
        return chatMessageMapper.selectUnsyncedMessages(sessionId);
    }

    @Override
    public List<ChatMessage> selectAllValidMessages(String sessionId) {
        return chatMessageMapper.selectAllValidMessages(sessionId);
    }

}

