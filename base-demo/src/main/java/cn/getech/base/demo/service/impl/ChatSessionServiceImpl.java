package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatSession;
import cn.getech.base.demo.enums.ChatSessionStatusEnum;
import cn.getech.base.demo.enums.ChatSessionTypeEnum;
import cn.getech.base.demo.mapper.ChatSessionMapper;
import cn.getech.base.demo.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static cn.getech.base.demo.constant.RedisKeyConstant.SESSION_ACTIVE;

/**
 * @author 11030
 */
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Override
    public void createOrUpdateChatSession(CustomerServiceStateDto state) {
        // 插入或更新会话
        ChatSession session = chatSessionMapper.selectBySessionId(state.getSessionId());
        if (session == null) {
            session = new ChatSession();
            session.setSessionId(state.getSessionId());
            session.setUserId(state.getUserId());
            session.setUserName(state.getUserName());
            session.setSessionType(ChatSessionTypeEnum.CONSULTATION.getCode());
            session.setStatus(ChatSessionStatusEnum.ACTIVE.getCode());
            session.setStartTime(LocalDateTime.now());
            session.setMessageCount(0);
            session.setCreateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
        } else {
            session.setLastMessageTime(LocalDateTime.now());
            chatSessionMapper.updateById(session);
        }
    }

}
