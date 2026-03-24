package cn.example.base.demo.service.impl;

import cn.example.base.demo.dto.CustomerServiceStateDto;
import cn.example.base.demo.entity.ChatSession;
import cn.example.base.demo.enums.ChatSessionStatusEnum;
import cn.example.base.demo.enums.ChatSessionTypeEnum;
import cn.example.base.demo.mapper.ChatSessionMapper;
import cn.example.base.demo.service.ChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * @author 11030
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Override
    public void createOrUpdateChatSession(CustomerServiceStateDto state) {
        // 插入或更新会话
        ChatSession session = baseMapper.selectBySessionId(state.getSessionId());
        if (session == null) {
            session = new ChatSession();
            session.setSessionId(state.getSessionId());
            session.setUserId(state.getUserId());
            session.setUserName(state.getUserName());
            session.setSessionType(ChatSessionTypeEnum.CONSULTATION.getId());
            session.setStatus(ChatSessionStatusEnum.ACTIVE.getId());
            session.setStartTime(LocalDateTime.now());
            session.setMessageCount(0);
            session.setCreateTime(LocalDateTime.now());
            baseMapper.insert(session);
        } else {
            session.setLastMessageTime(LocalDateTime.now());
            baseMapper.updateById(session);
        }
    }

}
