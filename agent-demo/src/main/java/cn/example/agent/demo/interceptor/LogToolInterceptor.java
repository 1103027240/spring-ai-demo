package cn.example.agent.demo.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogToolInterceptor extends ToolInterceptor {

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        log.info("【LogToolInterceptor】开始执行");
        long startTime = System.currentTimeMillis();

        ToolCallResponse response = handler.call(request);
        log.info("【LogToolInterceptor】调用耗时: {}ms", System.currentTimeMillis() - startTime);
        return response;
    }

    @Override
    public String getName() {
        return "LogToolInterceptor";
    }

}
