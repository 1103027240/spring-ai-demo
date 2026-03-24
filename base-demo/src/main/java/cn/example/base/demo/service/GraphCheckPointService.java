package cn.example.base.demo.service;

import cn.example.base.demo.dto.CustomerServiceStateDto;

public interface GraphCheckPointService {

    void deleteByThreadId(CustomerServiceStateDto state);

}
