package cn.getech.base.demo.service;

/**
 * 格式化处理器接口
 * @author 11030
 */
public interface TextSplitterFormatProcessor {
    /**
     * 处理最终的块文本
     * @param text 原始文本
     * @return 处理后的文本
     */
    String process(String text);

    /**
     * 向 StringBuilder 添加部分文本
     * @param sb StringBuilder
     * @param section 部分文本
     */
    void append(StringBuilder sb, String section);

}
