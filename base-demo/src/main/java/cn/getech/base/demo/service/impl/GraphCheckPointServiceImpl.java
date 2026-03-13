package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CustomerServiceStateDto;
import cn.getech.base.demo.mapper.GraphCheckPointMapper;
import cn.getech.base.demo.mapper.GraphThreadMapper;
import cn.getech.base.demo.service.GraphCheckPointService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class GraphCheckPointServiceImpl implements GraphCheckPointService {

    @Autowired
    private GraphThreadMapper graphThreadMapper;
    @Autowired
    private GraphCheckPointMapper graphCheckPointMapper;

    @Transactional
    @Override
    public void deleteByThreadId(CustomerServiceStateDto state) {
        if(Objects.nonNull(state) && StrUtil.isNotBlank(state.getExecutionId())){
            graphThreadMapper.deleteById(state.getExecutionId());
            graphCheckPointMapper.deleteByThreadId(state.getExecutionId());
        }
    }

}
