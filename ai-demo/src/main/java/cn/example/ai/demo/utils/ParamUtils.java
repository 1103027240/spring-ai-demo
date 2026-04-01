package cn.example.ai.demo.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

import static cn.example.ai.demo.constant.PatternConstant.MARKDOWN_CODE_BLOCK_PATTERN;

@Slf4j
public class ParamUtils {

    /**
     * 通用枚举解析方法
     */
    public static <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.debug("【枚举解析】{}的枚举值 '{}' 不存在", enumClass.getSimpleName(), value);
            return null;
        }
    }

    /**
     * 安全获取查询参数（指定类型）
     */
    public static <T> T getQueryParam(Map<String, Object> params, String key, Class<T> type) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }

        try {
            if (type.isInstance(value)) {
                return (T) value;
            }

            // 特殊处理：String 到基本类型的转换
            if (type == Integer.class || type == int.class) {
                return (T) Integer.valueOf(value.toString());
            }
            if (type == Long.class || type == long.class) {
                return (T) Long.valueOf(value.toString());
            }
            if (type == Double.class || type == double.class) {
                return (T) Double.valueOf(value.toString());
            }
            log.debug("【参数获取】类型不匹配，期望: {}, 实际: {}, 值: {}", type, value.getClass(), value);
            return null;
        } catch (Exception e) {
            log.warn("【参数获取】参数转换失败，键: {}, 值: {}, 目标类型: {}", key, value, type);
            return null;
        }
    }

    /**
     * 将有效值放入Map
     */
    public static void putIfValid(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.containsKey(key) && source.get(key) != null) {
            target.put(key, source.get(key));
        }
    }

    /**
     * 复制有效参数
     */
    public static void putIfValid(Map<String, Object> source, Map<String, Object> target, String[] keys) {
        for (String key : keys) {
            if (source.containsKey(key) && source.get(key) != null) {
                target.put(key, source.get(key));
            }
        }
    }

    /**
     * 清理markdown代码块，移除 ```json 和 ``` 等标记，保留纯JSON内容
     */
    public static String cleanMarkdownCodeBlock(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        String result = MARKDOWN_CODE_BLOCK_PATTERN.matcher(text).replaceAll("").trim();
        log.debug("清理markdown后内容: {}", result);
        return result;
    }

}
