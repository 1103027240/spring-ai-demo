package cn.getech.base.demo.tools;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
public class ParamUtils {

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
    public static void copyValidParams(Map<String, Object> source, Map<String, Object> target, String[] keys) {
        for (String key : keys) {
            if (source.containsKey(key) && source.get(key) != null) {
                target.put(key, source.get(key));
            }
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

}
