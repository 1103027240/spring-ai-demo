package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.GraphThread;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author 11030
 */
@Mapper
public interface GraphThreadMapper extends BaseMapper<GraphThread> {

    @Select("SELECT * FROM GRAPH_THREAD WHERE thread_name = #{threadName} limit 1")
    GraphThread getByThreadName(String threadName);

}
