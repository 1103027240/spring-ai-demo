package cn.example.base.demo.service.impl;

import cn.example.base.demo.dto.CustomerServiceStateDto;
import cn.example.base.demo.entity.GraphCheckPoint;
import cn.example.base.demo.entity.GraphThread;
import cn.example.base.demo.mapper.GraphCheckPointMapper;
import cn.example.base.demo.mapper.GraphThreadMapper;
import cn.example.base.demo.service.GraphCheckPointService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
