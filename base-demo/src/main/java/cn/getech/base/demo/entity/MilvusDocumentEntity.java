package cn.getech.base.demo.entity;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @author 11030
 */
@Data
@SuperBuilder
public class MilvusDocumentEntity {

    /**
     * 文档唯一ID（Milvus自动生成）
     */
    private String docId;

    /**
     * 记忆摘要文本内容
     */
    private String content;

    /**
     * 1024维向量（由EmbeddingModel自动生成）
     */
    private float[] embedding;

}
