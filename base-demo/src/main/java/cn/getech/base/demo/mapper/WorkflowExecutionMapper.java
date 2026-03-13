package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.WorkflowExecution;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
@CacheNamespace(flushInterval = 60000, size = 512)
public interface WorkflowExecutionMapper extends BaseMapper<WorkflowExecution> {

}