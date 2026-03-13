package cn.getech.base.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

import static cn.getech.base.demo.enums.KnowledgeDocumentStatusEnum.ENABLED;

/**
 * @author 11030
 */
@Data
@TableName("knowledge_document")
@Accessors(chain = true)
public class KnowledgeDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("category")
    private String category;

    @TableField("tags")
    private String tags;

    @TableField("source")
    private String source;

    @TableField("priority")
    private Integer priority = 0;

    /**
     * 是否已向量化：0-否, 1-是
     */
    @TableField("is_vectorized")
    private Integer isVectorized = 0;

    @TableField("vectorId")
    private String vectorId;

    @TableField("version")
    private Integer version = 1;

    @TableField("status")
    private Integer status = 1;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 是否已启用
     */
    public boolean isEnabled() {
        return ENABLED.equals(status);
    }

    /**
     * 是否已向量化
     */
    public boolean isVectorized() {
        return isVectorized != null && isVectorized == 1;
    }

    /**
     * 获取内容摘要（前100个字符）
     */
    public String getContentSummary() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        int length = Math.min(content.length(), 100);
        return content.substring(0, length) + (content.length() > 100 ? "..." : "");
    }

}
