package cn.getech.base.demo.service;

import cn.getech.base.demo.dto.CustomerServiceStateDto;

public interface GraphCheckPointService {

    void deleteByThreadId(CustomerServiceStateDto state);

}
