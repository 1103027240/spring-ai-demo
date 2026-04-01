package cn.example.ai.demo.service;

import cn.example.ai.demo.param.vo.ContactInfoVO;

public interface AgentStructuredDataService {

    ContactInfoVO doChat(String message);

}
