package cn.example.base.demo.build;

import cn.example.base.demo.utils.ParamUtils;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WorkflowBuild {

    @Autowired
    private ObjectMapper objectMapper;

    public static String generateExecutionId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
    }

    public static String generateWorkflowId() {
        return UUID.randomUUID().toString().replace("-", "");
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
