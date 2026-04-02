package cn.example.agent.demo.service;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;

import java.util.Map;

public interface SimpleCustomerChatLoopService {

    Map<String, Object> runLoop(Long userId, String sessionId, String message) throws GraphRunnerException;

}
