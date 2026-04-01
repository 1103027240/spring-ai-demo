package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.service.MultiSkillService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import static cn.example.ai.demo.constant.FieldConstant.DATA;
import static cn.example.ai.demo.constant.FieldConstant.SUCCESS;

@Slf4j
@Service
public class MultiSkillServiceImpl implements MultiSkillService {

    @Autowired
    private ReActAgent demoSqlAssistantAgent;

    @Autowired
    private ReActAgent demoInventoryManagementAgent;

    @Autowired
    private ReActAgent demoSalesAnalysisAgent;

    @Override
    public Map<String, Object> doChatSqlAssistant(String message) {
        Msg msg = Msg.builder().content(TextBlock.builder().text(message).build()).build();
        String result = demoSqlAssistantAgent.call(msg).block().getTextContent();
        return Map.of(SUCCESS, true, DATA, result);
    }

    @Override
    public Map<String, Object> doChatInventoryManagement(String message) {
        Msg msg = Msg.builder().content(TextBlock.builder().text(message).build()).build();
        String result = demoInventoryManagementAgent.call(msg).block().getTextContent();
        return Map.of(SUCCESS, true, DATA, result);
    }

    @Override
    public Map<String, Object> doyChatSalesAnalysis(String message) {
        Msg msg = Msg.builder().content(TextBlock.builder().text(message).build()).build();
        String result = demoSalesAnalysisAgent.call(msg).block().getTextContent();
        return Map.of(SUCCESS, true, DATA, result);
    }

}
