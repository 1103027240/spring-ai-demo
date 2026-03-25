package cn.example.base.demo.service;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;

import java.util.Map;

public interface ReturnProcessSequentialService {

    Map<String, Object> runSequential(String orderId) throws GraphRunnerException;

}
