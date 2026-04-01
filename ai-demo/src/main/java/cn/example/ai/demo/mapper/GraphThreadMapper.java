package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.GraphThread;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author 11030
 */
@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface GraphThreadMapper extends BaseMapper<GraphThread> {

    @Select("SELECT * FROM GRAPH_THREAD WHERE thread_name = #{threadName} limit 1")
    GraphThread getByThreadName(String threadName);

}
