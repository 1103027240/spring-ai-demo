package cn.example.ai.demo.mapper;

import cn.example.ai.demo.entity.WorkflowExecution;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface WorkflowExecutionMapper extends BaseMapper<WorkflowExecution> {

}