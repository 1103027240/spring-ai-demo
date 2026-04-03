package cn.example.agent.demo.service;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import java.util.Map;

public interface CustomerVerificationParallelService {

    Map<String, Object> runParallel(String customerId) throws GraphRunnerException;

}
