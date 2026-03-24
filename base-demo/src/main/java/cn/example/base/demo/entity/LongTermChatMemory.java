package cn.example.base.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * milvus数据库中long_term_memory集合
 * @author 11030
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LongTermChatMemory extends MilvusDocument {

    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 创建时间戳
     */
    private String createTime;
    
    /**
     * 记忆类型（LONG_TERM）
     */
    private String memoryType;

}
