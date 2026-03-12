package cn.getech.base.demo.entity;

import cn.getech.base.demo.enums.KnowledgeDocumentStatusEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static cn.getech.base.demo.enums.KnowledgeDocumentStatusEnum.ENABLED;

/**
 * @author 11030
 */
@Data
@TableName("knowledge_document")
@Accessors(chain = true)
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String summary;

    private String category;

    private String tags;

    private String source;

    private Integer priority = 0;

    /**
     * 是否已向量化：0-否, 1-是
     */
    @TableField("is_vectorized")
    private Integer isVectorized = 0;

    @TableField("vector_id")
    private String vectorId;

    private Integer version = 1;

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

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("contentLength", content != null ? content.length() : 0);
        map.put("contentSummary", getContentSummary());
        map.put("summary", summary);
        map.put("category", category);
        map.put("tags", tags);
        map.put("source", source);
        map.put("priority", priority);
        map.put("isVectorized", isVectorized);
        map.put("isVectorized_bool", isVectorized());
        map.put("vectorId", vectorId);
        map.put("version", version);
        map.put("status", status);
        map.put("statusText", KnowledgeDocumentStatusEnum.getDescription(status));
        map.put("isEnabled", isEnabled());
        map.put("createTime", createTime);
        map.put("updateTime", updateTime);
        return map;
    }

}
