package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    @Select("SELECT * FROM chat_session WHERE session_id = #{sessionId}")
    ChatSession selectBySessionId(@Param("sessionId") String sessionId);

}
