package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.ExportTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface ExportTaskMapper extends BaseMapper<ExportTask> {

    @Select("SELECT * FROM export_task WHERE create_time <= #{expireTime}")
    List<ExportTask> selectExpiredTasks(LocalDateTime expireTime);

}
