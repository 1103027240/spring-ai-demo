package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.build.ChatMessageBuild;
import cn.example.agent.demo.entity.ChatMessage;
import cn.example.agent.demo.mapper.ChatMessageMapper;
import cn.example.agent.demo.service.ChatMessageService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ChatMessageBuild chatMessageBuild;

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

}

