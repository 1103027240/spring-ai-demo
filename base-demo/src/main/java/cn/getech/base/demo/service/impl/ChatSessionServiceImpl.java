package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatSession;
import cn.getech.base.demo.enums.ChatSessionStatusEnum;
import cn.getech.base.demo.enums.ChatSessionTypeEnum;
import cn.getech.base.demo.mapper.ChatSessionMapper;
import cn.getech.base.demo.service.ChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
