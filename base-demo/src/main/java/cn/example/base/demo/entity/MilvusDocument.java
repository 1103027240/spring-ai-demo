package cn.example.base.demo.entity;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @author 11030
 */
@Data
@SuperBuilder
public class MilvusDocument {

    /**
     * 文档ID
     */
    private String docId;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 向量
     */
    private Float[] embedding;

}
