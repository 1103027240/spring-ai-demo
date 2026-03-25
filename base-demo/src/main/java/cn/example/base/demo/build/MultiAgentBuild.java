package cn.example.base.demo.build;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MultiAgentBuild {

    public String extractText(Map<String, Object> dataMap, String key) {
        Object value = dataMap.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Message message) {
            return message.getText();
        }
        return value != null ? value.toString() : null;
    }

}
