package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.MessageSyncTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.CacheNamespace;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface MessageSyncTaskMapper extends BaseMapper<MessageSyncTask> {

    @Select("SELECT * FROM message_sync_task WHERE session_id = #{sessionId} AND status = 0 ORDER BY created_time ASC LIMIT 1")
    MessageSyncTask selectLatestPendingTask(@Param("sessionId") String sessionId);

}