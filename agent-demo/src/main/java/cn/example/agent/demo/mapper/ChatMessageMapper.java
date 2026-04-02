package cn.example.agent.demo.mapper;

import cn.example.agent.demo.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} AND session_id = #{sessionId} AND is_deleted = 0 ORDER BY create_time DESC, is_ai_response DESC LIMIT #{limit}")
    List<ChatMessage> selectHistoryByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId, @Param("limit") int limit);

}
