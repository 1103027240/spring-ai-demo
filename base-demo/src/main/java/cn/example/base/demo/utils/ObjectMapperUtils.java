package cn.example.base.demo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ObjectMapperUtils {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 转换列表为JSON
     */
    public String convertToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("转换列表为JSON失败", e);
            return "[]";
        }
    }

    /**
     * 转换Map为JSON
     */
    public String convertToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("转换Map为JSON失败", e);
            return "{}";
        }
    }

}
