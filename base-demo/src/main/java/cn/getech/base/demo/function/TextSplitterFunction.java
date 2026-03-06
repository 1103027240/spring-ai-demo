package cn.getech.base.demo.function;

import java.util.List;

/**
 * 文本分割函数接口
 * @author 11030
 */
@FunctionalInterface
public interface TextSplitterFunction {

    List<String> apply(String text);

}
