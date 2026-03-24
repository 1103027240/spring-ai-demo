package cn.example.base.demo.mapper;

import cn.example.base.demo.entity.GraphCheckPoint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 11030
 */
@Mapper
public interface GraphCheckPointMapper extends BaseMapper<GraphCheckPoint> {

    @Delete("DELETE FROM GRAPH_CHECKPOINT WHERE thread_id = #{threadId}")
    int deleteByThreadId(String threadId);

}
