package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.hook.LogAgentHook;
import cn.example.agent.demo.hook.StatisticsModelHook;
import cn.example.agent.demo.hook.TrimmingModelHook;
import cn.example.agent.demo.interceptor.LogModelInterceptor;
import cn.example.agent.demo.interceptor.LogToolInterceptor;
import cn.example.agent.demo.service.AgentHookService;
import cn.example.agent.demo.tools.CalculatorTools;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class AgentHookServiceImpl implements AgentHookService {

    @Resource(name = "qwenChatModel")
    private ChatModel qwenChatModel;

    @Override
    public String doChat(String message) {
        try {
            StatisticsModelHook statisticsModelHook = new StatisticsModelHook();
            TrimmingModelHook trimmingModelHook = new TrimmingModelHook();
            LogAgentHook logAgentHook = new LogAgentHook();

            LogModelInterceptor logModelInterceptor = new LogModelInterceptor();
            LogToolInterceptor logToolInterceptor = new LogToolInterceptor();

            CalculatorTools calculatorTools = new CalculatorTools();

            ReactAgent agent = ReactAgent.builder()
                    .name("钩子智能体")
                    .model(qwenChatModel)
                    .instruction("""
                        你是一个AI助手，可以回答用户的问题。
                        
                        可用工具列表：
                        - getWeatherV3: 获取城市天气，参数为城市名称字符串
                        - add: 两数相加
                        - subtract: 两数相减
                        
                        用户问题: {message}
                        
                        重要规则：
                        1. 你必须始终使用中文回复
                        """)
                    .methodTools(calculatorTools)
                    .hooks(statisticsModelHook, trimmingModelHook, logAgentHook)
                    .interceptors(logModelInterceptor, logToolInterceptor)
                    .build();

            return agent.call(Map.of("message", message, "userId", "10001")).getText();
        } catch (GraphRunnerException e) {
            log.error("钩子智能体对话执行报错", e);
            throw new RuntimeException(e);
        }
    }

}
