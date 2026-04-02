package cn.example.agent.demo.service.impl;

import cn.example.agent.demo.service.MultiSkillService;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import static cn.example.agent.demo.constant.FieldConstant.*;

@Slf4j
@Service
public class MultiSkillServiceImpl implements MultiSkillService {

    @Resource(name = "demoSqlAssistantAgent")
    private ReactAgent demoSqlAssistantAgent;

    @Resource(name = "demoInventoryManagementAgent")
    private ReactAgent demoInventoryManagementAgent;

    @Resource(name = "demoSalesAnalysisAgent")
    private ReactAgent demoSalesAnalysisAgent;

    @Override
    public Map<String, Object> doChatSqlAssistant(String message) {
        try {
            String result = demoSqlAssistantAgent.call(Map.of(MESSAGE, message)).getText();
            return Map.of(SUCCESS, true, DATA, result);
        } catch (GraphRunnerException e) {
            log.error("【Sql技能包使用】执行失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> doChatInventoryManagement(String message) {
        try {
            String result = demoInventoryManagementAgent.call(Map.of(MESSAGE, message)).getText();
            return Map.of(SUCCESS, true, DATA, result);
        } catch (GraphRunnerException e) {
            log.error("【库存管理技能包使用】执行失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> doChatSalesAnalysis(String message) {
        try {
            String result = demoSalesAnalysisAgent.call(Map.of(MESSAGE, message)).getText();
            return Map.of(SUCCESS, true, DATA, result);
        } catch (GraphRunnerException e) {
            log.error("【销售分析技能包使用】执行失败", e);
            throw new RuntimeException(e);
        }
    }

}
