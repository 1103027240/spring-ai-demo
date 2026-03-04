package cn.getech.spring.ai.demo.converter;

import org.springframework.ai.document.Document;
import java.util.Map;
import java.util.function.Function;

/**
 * 任意对象与 Document 转换
 * @author 11030
 */
public class DocumentConverter {

    /**
     * 将任意对象转换为 Document
     * @param content 文档内容
     * @param metadataFunction 元数据提取函数
     * @return Document
     */
    public static <T> Document toDocument(String content, Function<T, Map<String, Object>> metadataFunction) {
        return new Document(content, metadataFunction.apply(null));
    }

    /**
     * 将任意对象转换为 Document（带完整参数）
     * @param entity 源对象
     * @param contentFunction 内容提取函数
     * @param metadataFunction 元数据提取函数
     * @return Document
     */
    public static <T> Document toDocument(T entity, Function<T, String> contentFunction, Function<T, Map<String, Object>> metadataFunction) {
        String content = contentFunction.apply(entity);
        return toDocument(content, metadataFunction);
    }

    /**
     * 将 Document 转换为任意对象
     * @param document 源 Document
     * @param entityBuilder 实体构建函数
     * @return 构建的实体
     */
    public static <T> T toEntity(Document document, DocumentToEntityMapper<T> entityBuilder) {
        return entityBuilder.map(document.getText(), document.getMetadata());
    }

    /**
     * 实体构建器接口
     */
    @FunctionalInterface
    public interface DocumentToEntityMapper<T> {
        T map(String content, Map<String, Object> metadata);
    }

}
