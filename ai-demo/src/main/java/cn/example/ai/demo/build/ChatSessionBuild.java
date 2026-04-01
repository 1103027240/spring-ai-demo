package cn.example.ai.demo.build;

import cn.example.ai.demo.param.dto.CustomerServiceStateDto;
import cn.example.ai.demo.entity.ChatSession;
import cn.example.ai.demo.enums.ChatSessionStatusEnum;
import cn.example.ai.demo.enums.ChatSessionTypeEnum;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ChatSessionBuild {

    public ChatSession buildAddChatSession(CustomerServiceStateDto state){
        ChatSession session = new ChatSession();
        session.setSessionId(state.getSessionId());
        session.setUserId(state.getUserId());
        session.setUserName(state.getUserName());
        session.setSessionType(ChatSessionTypeEnum.CONSULTATION.getId());
        session.setStatus(ChatSessionStatusEnum.ACTIVE.getId());
        session.setStartTime(LocalDateTime.now());
        session.setMessageCount(0);
        session.setCreateTime(LocalDateTime.now());
        return session;
    }

}
