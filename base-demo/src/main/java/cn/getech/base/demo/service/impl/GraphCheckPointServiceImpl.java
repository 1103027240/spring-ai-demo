package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.entity.ChatSession;
import cn.getech.base.demo.entity.GraphCheckPoint;
import cn.getech.base.demo.entity.GraphThread;
import cn.getech.base.demo.mapper.ChatSessionMapper;
import cn.getech.base.demo.mapper.GraphCheckPointMapper;
import cn.getech.base.demo.mapper.GraphThreadMapper;
import cn.getech.base.demo.service.GraphCheckPointService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class GraphCheckPointServiceImpl extends ServiceImpl<GraphCheckPointMapper, GraphCheckPoint> implements GraphCheckPointService {

    @Autowired
    private GraphThreadMapper graphThreadMapper;

    @Transactional
    @Override
    public void deleteByThreadId(CustomerServiceStateDto state) {
        if (Objects.nonNull(state) && StrUtil.isNotBlank(state.getExecutionId())) {
            GraphThread graphThread = graphThreadMapper.getByThreadName(state.getExecutionId());
            if (Objects.nonNull(graphThread)) {
                baseMapper.deleteByThreadId(graphThread.getThreadId());
                graphThreadMapper.deleteById(graphThread.getThreadId());
            }
        }
    }

}
