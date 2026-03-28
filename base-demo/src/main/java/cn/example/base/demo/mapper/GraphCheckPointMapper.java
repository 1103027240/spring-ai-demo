package cn.example.base.demo.mapper;

import cn.example.base.demo.entity.GraphCheckPoint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 11030
 */
@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface GraphCheckPointMapper extends BaseMapper<GraphCheckPoint> {

    @Delete("DELETE FROM GRAPH_CHECKPOINT WHERE thread_id = #{threadId}")
    int deleteByThreadId(String threadId);

}
