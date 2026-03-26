package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.ChatSessionBuild;
import cn.example.base.demo.dto.CustomerServiceStateDto;
import cn.example.base.demo.entity.ChatSession;
import cn.example.base.demo.mapper.ChatSessionMapper;
import cn.example.base.demo.service.ChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * @author 11030
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Autowired
    private ChatSessionBuild chatSessionBuild;

    @Override
    public void createOrUpdateChatSession(CustomerServiceStateDto state) {
        // 插入或更新会话
        ChatSession session = baseMapper.selectBySessionId(state.getSessionId());
        if (session == null) {
            session = chatSessionBuild.buildAddChatSession(state);
            baseMapper.insert(session);
        } else {
            session.setLastMessageTime(LocalDateTime.now());
            baseMapper.updateById(session);
        }
    }

}
