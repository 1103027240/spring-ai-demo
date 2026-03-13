package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.MessageSyncTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.CacheNamespace;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
@CacheNamespace(flushInterval = 60000, size = 512)
public interface MessageSyncTaskMapper extends BaseMapper<MessageSyncTask> {

    @Select("SELECT * FROM message_sync_task WHERE session_id = #{sessionId} AND status = 0 ORDER BY created_time ASC LIMIT 1")
    MessageSyncTask selectLatestPendingTask(@Param("sessionId") String sessionId);

}