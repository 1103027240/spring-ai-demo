package cn.getech.base.demo.utils;

import org.springframework.ai.document.Document;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Document 工具类
 */
public class DocumentUtils {

    /**
     * 过滤 Map 中的 null 值
     * 使用 Stream API，业务代码中无需显式判断
     */
    public static Map<String, Object> filterNullValues(Map<String, Object> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> newVal,
                        HashMap::new
                ));
    }

    /**
     * 创建 Document，自动过滤 metadata 中的 null 值
     */
    public static Document createDocument(String id, String text, Map<String, Object> metadata) {
        return Document.builder()
                .id(id)
                .text(text)
                .metadata(filterNullValues(metadata))
                .build();
    }

}
