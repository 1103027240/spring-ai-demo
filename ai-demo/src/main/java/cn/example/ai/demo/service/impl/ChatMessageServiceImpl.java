package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.build.ChatMessageBuild;
import cn.example.ai.demo.param.dto.CustomerServiceStateDto;
import cn.example.ai.demo.entity.ChatMessage;
import cn.example.ai.demo.mapper.ChatMessageMapper;
import cn.example.ai.demo.service.ChatMessageService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Value("${sync.mysql.retention.count:20}")
    private String mysqlRetentionCount;

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

        if (CollUtil.isNotEmpty(messages)) {
            saveBatch(messages);
        }
        return messages;
    }

    @Override
    public List<ChatMessage> batchSaveMessages(Long userId, String sessionId, String userMessage, String aiResponse) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessageBuild.buildUserChatMessage(sessionId, userId, userMessage));
        messages.add(chatMessageBuild.buildAiChatMessage(sessionId, userId, aiResponse));

        if (CollUtil.isNotEmpty(messages)) {
            saveBatch(messages);
        }
        return messages;
    }

    /**
     * 清理Mysql中的会话消息
     */
    @Override
    public void cleanupOldMessageInMysql(Long userId) {
        int mysqlRetentionMaxCount = Integer.parseInt(mysqlRetentionCount);
        int messageCount = baseMapper.countByUserId(userId);
        int messagesDelete = messageCount - mysqlRetentionMaxCount;
        if (messagesDelete > 0) {
            int deletedCount = baseMapper.deleteOldMessages(userId, messagesDelete);
            log.info("【清理旧消息】userId: {}, 删除了 {} 条消息", userId, deletedCount);
        }
    }

    @Override
    public void updateMessageSyncStatus(String sessionId) {
        try {
            int updatedCount = baseMapper.updateSyncStatusToSynced(sessionId);
            log.info("【异步同步】更新消息同步状态为已完成，sessionId: {}, 更新数量: {}", sessionId, updatedCount);
        } catch (Exception e) {
            log.error("【异步同步】更新消息同步状态失败", e);
        }
    }

    @Override
    public List<ChatMessage> selectUnsyncedMessages(String sessionId) {
        return baseMapper.selectUnsyncedMessages(sessionId);
    }

    @Override
    public List<ChatMessage> selectAllValidMessages(String sessionId) {
        return baseMapper.selectAllValidMessages(sessionId);
    }

}

