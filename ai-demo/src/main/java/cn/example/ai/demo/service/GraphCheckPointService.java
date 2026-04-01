package cn.example.ai.demo.service;

import cn.example.ai.demo.param.dto.CustomerServiceStateDto;

public interface GraphCheckPointService {

    void deleteByThreadId(CustomerServiceStateDto state);

}
