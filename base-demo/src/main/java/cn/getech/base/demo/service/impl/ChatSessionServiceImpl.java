package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.ChatSession;
import cn.getech.base.demo.enums.ChatSessionStatusEnum;
import cn.getech.base.demo.enums.ChatSessionTypeEnum;
import cn.getech.base.demo.mapper.ChatSessionMapper;
import cn.getech.base.demo.service.ChatSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static cn.getech.base.demo.contant.RedisKeyConstants.SESSION_ACTIVE;

/**
 * @author 11030
 */
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void createOrUpdateChatSession(String sessionId, Long userId, String userName) throws JsonProcessingException {
        // 插入或更新会话
        ChatSession session = chatSessionMapper.selectBySessionId(sessionId);
        if (session == null) {
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setUserName(userName);
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

        // 缓存活跃会话
        cacheActiveSession(sessionId);
    }

    private void cacheActiveSession(String sessionId) throws JsonProcessingException {
        String sessionKey = SESSION_ACTIVE + sessionId;
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", sessionId);
        sessionInfo.put("lastActiveTime", System.currentTimeMillis());
        String sessionJson = objectMapper.writeValueAsString(sessionInfo);
        //redisTemplate.opsForValue().set(sessionKey, sessionJson, 3600, TimeUnit.SECONDS);
    }

}
