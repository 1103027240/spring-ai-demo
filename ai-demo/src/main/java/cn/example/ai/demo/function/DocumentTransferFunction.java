package cn.example.ai.demo.function;

import java.util.Map;

/**
 * @author 11030
 */
@FunctionalInterface
public interface DocumentTransferFunction<T> {

    T map(String content, Map<String, Object> metadata);

}
