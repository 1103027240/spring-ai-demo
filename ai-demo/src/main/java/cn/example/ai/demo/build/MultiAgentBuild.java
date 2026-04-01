package cn.example.ai.demo.build;

import cn.example.ai.demo.utils.ParamUtils;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MultiAgentBuild {

    @Autowired
    private ObjectMapper objectMapper;

    public String extractText(Map<String, Object> dataMap, String key) {
        Object value = dataMap.get(key);
        if (value == null) {
            return null;
        }
        return extractText(value);
    }

    public String extractText(Object value) {
        if(value == null){
            return null;
        }

        if (value instanceof Message message) {
            return message.getText();
        }

        return value != null ? value.toString() : null;
    }

    public Map<String, Object> parseMap(Object result) {
        if(result == null){
            return new HashMap<>();
        }

        if (result instanceof String str) {
            return parseJsonResponse(str);
        }
        else if (result instanceof Map map) {
            return map;
        }
        else {
            return (Map<String, Object>) result;
        }
    }

    public Map<String, Object> parseJsonResponse(String json) {
        try {
            if (StrUtil.isBlank(json)) {
                return new HashMap<>();
            }

            String jsonStr = ParamUtils.cleanMarkdownCodeBlock(json);
            return objectMapper.readValue(jsonStr, Map.class);
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "解析JSON失败: " + e.getMessage());
            return result;
        }
    }

}
