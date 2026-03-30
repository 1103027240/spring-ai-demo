package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.MultiSkillService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static cn.example.base.demo.constant.FieldConstant.DATA;
import static cn.example.base.demo.constant.FieldValueConstant.THREE_HUNDRED;
import static cn.example.base.demo.constant.RedisKeyConstant.SKILL_DEMO_PREFIX;

@Slf4j
@Service
public class MultiSkillServiceImpl implements MultiSkillService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ReActAgent demoSqlAssistantAgent;

    @Autowired
    private ReActAgent demoInventoryManagementAgent;

    @Autowired
    private ReActAgent demoSalesAnalysisAgent;

    @Override
    public Map<String, Object> doChatSqlAssistant(String message) {
        String cacheKey = SKILL_DEMO_PREFIX + "sql:" + message.hashCode();
        return queryWithCache(cacheKey, message, demoSqlAssistantAgent);
    }

    @Override
    public Map<String, Object> doChatInventoryManagement(String message) {
        String cacheKey = SKILL_DEMO_PREFIX + "inventory:" + message.hashCode();
        return queryWithCache(cacheKey, message, demoInventoryManagementAgent);
    }

    @Override
    public Map<String, Object> doyChatSalesAnalysis(String message) {
        String cacheKey = SKILL_DEMO_PREFIX + "sales:" + message.hashCode();
        return queryWithCache(cacheKey, message, demoSalesAnalysisAgent);
    }

    private Map<String, Object> queryWithCache(String cacheKey, String message, ReActAgent agent) {
        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Map.of(DATA, cached, "fromCache", true);
        }
        
        // 2. 调用Agent
        long start = System.currentTimeMillis();

        Msg msg = Msg.builder().content(TextBlock.builder().text(message).build()).build();
        String result = agent.call(msg).block().getTextContent();

        long cost = System.currentTimeMillis() - start;
        log.info("Agent调用耗时: {}ms, 结果长度: {}", cost, result.length());
        
        // 3. 写缓存
        redisTemplate.opsForValue().set(cacheKey, result, THREE_HUNDRED, TimeUnit.SECONDS);
        return Map.of(DATA, result, "fromCache", false, "costMs", cost);
    }

}
