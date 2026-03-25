package cn.example.base.demo.service;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;

import java.util.Map;

public interface SimpleChatLoopService {

    Map<String, Object> doChat(String userId, String sessionId, String message) throws GraphRunnerException;

}
