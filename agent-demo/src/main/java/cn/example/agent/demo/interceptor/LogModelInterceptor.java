package cn.example.agent.demo.interceptor;

import cn.example.agent.demo.build.ToolCallbackBuild;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

@Slf4j
public class LogModelInterceptor extends ModelInterceptor {

    @Autowired
    private ToolCallbackBuild toolCallbackBuild;

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        log.info("【LogModelInterceptor】开始执行");
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = request.getContext();
        String userId = (String) context.get("userId");
        ModelResponse response;

        if("10001".equals(userId)){
            ModelRequest newRequest = ModelRequest.builder(request)
                    .dynamicToolCallbacks(List.of(toolCallbackBuild.getSimpleSqlTool()))
                    .build();

            response = handler.call(newRequest);
            log.info("【LogModelInterceptor】调用耗时: {}ms", System.currentTimeMillis() - startTime);
            return response;
        }

        response = handler.call(request);
        log.info("【LogModelInterceptor】调用耗时: {}ms", System.currentTimeMillis() - startTime);
        return response;
    }

    @Override
    public String getName() {
        return "LogModelInterceptor";
    }

}
