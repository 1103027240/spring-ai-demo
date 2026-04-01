package cn.example.ai.demo.service;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 文本分割服务接口
 * @author 11030
 */
public interface TextSplitterService {

    /**
     * 使用特定分割器分割文本
     * @param text 待分割文本
     * @param algorithm 分割算法
     * @param metadata 元数据
     * @return 分割后的文档列表
     */
    List<Document> split(String text, String algorithm, Map<String, Object> metadata);

    /**
     * 智能分割文本 - 自动选择最合适的分割器
     * @param text 待分割文本
     * @param metadata 元数据
     * @return 分割后的文档列表
     */
    List<Document> intelligentSplit(String text, Map<String, Object> metadata);

    /**
     * 批量分割文本
     * @param texts 文本映射，键为文档ID，值为文本内容
     * @param algorithm 分割算法
     * @return 分割结果映射，键为文档ID，值为分割后的文档列表
     */
    Map<String, List<Document>> batchSplit(Map<String, String> texts, String algorithm);

}