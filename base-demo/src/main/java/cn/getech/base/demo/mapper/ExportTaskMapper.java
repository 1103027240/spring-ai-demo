package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.ExportTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTask> {

    List<ExportTask> selectExpiredTasks(LocalDateTime expireTime);

}
