package cn.example.base.demo.service;

import cn.example.base.demo.param.dto.CustomerServiceStateDto;

public interface GraphCheckPointService {

    void deleteByThreadId(CustomerServiceStateDto state);

}
