package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND sync_status = 0 AND is_deleted = 0 ORDER BY id ASC")
    List<ChatMessage> selectUnsyncedMessages(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND is_deleted = 0 ORDER BY id ASC")
    List<ChatMessage> selectAllValidMessages(@Param("sessionId") String sessionId);

    @Select("SELECT COUNT(*) FROM chat_message WHERE user_id = #{userId} AND is_deleted = 0")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} AND session_id = #{sessionId} AND is_deleted = 0 ORDER BY create_time DESC, is_ai_response DESC LIMIT #{limit}")
    List<ChatMessage> selectHistoryByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId, @Param("limit") int limit);

    @Update("UPDATE chat_message SET sync_status = 2, sync_time = NOW() WHERE session_id = #{sessionId} AND sync_status = 0 AND is_deleted = 0")
    int updateSyncStatusToSynced(@Param("sessionId") String sessionId);

    @Update("""
            UPDATE chat_message SET is_deleted = 1 
            WHERE user_id = #{userId} 
            AND is_deleted = 0
            AND id IN (
                SELECT id FROM (
                    SELECT id FROM chat_message 
                    WHERE user_id = #{userId} AND is_deleted = 0 
                    ORDER BY id ASC 
                    LIMIT #{limit}
                ) as temp
            )
    """)
    int deleteOldMessages(@Param("userId") Long userId, @Param("limit") int limit);

}
