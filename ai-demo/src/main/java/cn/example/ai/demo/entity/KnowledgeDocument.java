package cn.example.ai.demo.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库文档实体类
 */
@Slf4j
@Data
@TableName("knowledge_document")
@Accessors(chain = true)
public class KnowledgeDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档标题
     */
    @TableField("title")
    private String title;

    /**
     * 文档内容
     */
    @TableField("content")
    private String content;

    /**
     * 文档摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 文档分类
     */
    @TableField("category")
    private String category;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 标签列表（JSON格式）
     */
    @TableField("tags")
    private String tags;

    /**
     * 关键词列表（JSON格式）
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 文档来源
     */
    @TableField("source")
    private String source;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 优先级（0-100，数值越大优先级越高）
     */
    @TableField("priority")
    private Integer priority = 0;

    /**
     * 状态：0-禁用，1-启用，2-待审核，3-已删除
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 查看次数
     */
    @TableField("view_count")
    private Integer viewCount = 0;

    /**
     * 有用次数
     */
    @TableField("useful_count")
    private Integer usefulCount = 0;

    /**
     * 无用次数
     */
    @TableField("useless_count")
    private Integer uselessCount = 0;

    /**
     * 是否已向量化：0-未向量化，1-已向量化，2-已修改但未向量化
     */
    @TableField("is_vectorized")
    private Integer isVectorized = 0;

    /**
     * 向量ID（在向量数据库中的唯一标识）
     */
    @TableField("vector_id")
    private String vectorId;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version = 1;

    /**
     * 元数据（JSON格式）
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 创建人名称
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Long createTime;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 更新人名称
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Long updateTime;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted = 0;

    /**
     * 相似度分数（用于向量搜索时返回，不映射到数据库）
     */
    @TableField(exist = false)
    private Double similarityScore;

    public KnowledgeDocument() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    public KnowledgeDocument(String title, String content, String category) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public KnowledgeDocument(String title, String content, String category, String summary, String author) {
        this(title, content, category);
        this.summary = summary;
        this.author = author;
    }

    /**
     * 增加查看次数
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * 增加有用次数
     */
    public void incrementUsefulCount() {
        this.usefulCount = (this.usefulCount == null ? 0 : this.usefulCount) + 1;
    }

    /**
     * 增加无用次数
     */
    public void incrementUselessCount() {
        this.uselessCount = (this.uselessCount == null ? 0 : this.uselessCount) + 1;
    }

    /**
     * 获取有用率
     */
    public double getUsefulRate() {
        int totalFeedback = (this.usefulCount == null ? 0 : this.usefulCount) + (this.uselessCount == null ? 0 : this.uselessCount);
        if (totalFeedback == 0) {
            return 0.0;
        }
        return (double) (this.usefulCount == null ? 0 : this.usefulCount) / totalFeedback;
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 是否禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
    }

    /**
     * 是否待审核
     */
    public boolean isPending() {
        return status != null && status == 2;
    }

    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return status != null && status == 3;
    }

    /**
     * 是否已向量化
     */
    public boolean isVectorized() {
        return isVectorized != null && isVectorized == 1;
    }

    /**
     * 标记为向量化
     */
    public void markAsVectorized(String vectorId) {
        this.isVectorized = 1;
        this.vectorId = vectorId;
    }

    /**
     * 标记为未向量化
     */
    public void markAsUnvectorized() {
        this.isVectorized = 0;
        this.vectorId = null;
    }

    /**
     * 更新版本
     */
    public void incrementVersion() {
        this.version = (this.version == null ? 1 : this.version) + 1;
    }

    /**
     * 获取标签列表
     */
    public List<String> getTagsList() {
        if (StrUtil.isBlank(tags)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(tags, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析标签JSON失败: {}", tags, e);
            return new ArrayList<>();
        }
    }

    /**
     * 设置标签列表
     */
    public void setTagsList(List<String> tagsList) {
        if (CollUtil.isEmpty(tagsList)) {
            this.tags = null;
        } else {
            try {
                this.tags = OBJECT_MAPPER.writeValueAsString(tagsList);
            } catch (Exception e) {
                log.warn("序列化标签列表失败", e);
                this.tags = "[]";
            }
        }
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        List<String> tagsList = getTagsList();
        if (!tagsList.contains(tag)) {
            tagsList.add(tag);
            setTagsList(tagsList);
        }
    }

    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        List<String> tagsList = getTagsList();
        tagsList.remove(tag);
        setTagsList(tagsList);
    }

    /**
     * 获取关键词列表
     */
    public List<String> getKeywordsList() {
        if (StrUtil.isBlank(keywords)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(keywords, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析关键词JSON失败: {}", keywords, e);
            return new ArrayList<>();
        }
    }

    /**
     * 设置关键词列表
     */
    public void setKeywordsList(List<String> keywordsList) {
        if (CollUtil.isEmpty(keywordsList)) {
            this.keywords = null;
        } else {
            try {
                this.keywords = OBJECT_MAPPER.writeValueAsString(keywordsList);
            } catch (Exception e) {
                log.warn("序列化关键词列表失败", e);
                this.keywords = "[]";
            }
        }
    }

    /**
     * 添加关键词
     */
    public void addKeyword(String keyword) {
        List<String> keywordsList = getKeywordsList();
        if (!keywordsList.contains(keyword)) {
            keywordsList.add(keyword);
            setKeywordsList(keywordsList);
        }
    }

    /**
     * 获取元数据
     */
    public Map<String, Object> getMetadataMap() {
        if (StrUtil.isBlank(metadata)) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(metadata, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析元数据JSON失败: {}", metadata, e);
            return new HashMap<>();
        }
    }

    /**
     * 设置元数据
     */
    public void setMetadataMap(Map<String, Object> metadataMap) {
        if (CollUtil.isEmpty(metadataMap)) {
            this.metadata = null;
        } else {
            try {
                this.metadata = OBJECT_MAPPER.writeValueAsString(metadataMap);
            } catch (Exception e) {
                log.warn("序列化元数据失败", e);
                this.metadata = "{}";
            }
        }
    }

    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        Map<String, Object> metadataMap = getMetadataMap();
        metadataMap.put(key, value);
        setMetadataMap(metadataMap);
    }

    /**
     * 获取元数据值
     */
    public Object getMetadataValue(String key) {
        return getMetadataMap().get(key);
    }

    /**
     * 获取内容摘要（如果summary为空，从content截取）
     */
    public String getContentSummary(int maxLength) {
        if (StringUtils.hasText(summary)) {
            return summary.length() > maxLength ? summary.substring(0, maxLength) + "..." : summary;
        }

        if (!StringUtils.hasText(content)) {
            return "";
        }

        // 清理HTML标签
        String plainContent = content.replaceAll("<[^>]+>", "");
        if (plainContent.length() > maxLength) {
            return plainContent.substring(0, maxLength) + "...";
        }
        return plainContent;
    }

    /**
     * 计算内容长度
     */
    public int getContentLength() {
        return content != null ? content.length() : 0;
    }

    /**
     * 计算字符数（中文字符算1个）
     */
    public int getCharacterCount() {
        if (StrUtil.isBlank(content)) {
            return 0;
        }
        return content.length();
    }

    /**
     * 计算预估阅读时间（以中文字符300字/分钟计算）
     */
    public int getEstimatedReadingTime() {
        int charCount = getCharacterCount();
        return (int) Math.ceil((double) charCount / 300);
    }

    /**
     * 校验文档是否有效
     */
    public boolean isValid() {
        return StringUtils.hasText(title) &&
                StringUtils.hasText(content) &&
                StringUtils.hasText(category) &&
                status != null && status == 1;
    }

    /**
     * 校验是否有足够的内容
     */
    public boolean hasSufficientContent(int minLength) {
        return getContentLength() >= minLength;
    }

    /**
     * 校验是否有有效的分类
     */
    public boolean hasValidCategory() {
        return StringUtils.hasText(category) && !"未知".equals(category);
    }

    /**
     * 转换为简化版本
     */
    public Map<String, Object> toSimpleMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("summary", summary);
        map.put("category", category);
        map.put("author", author);
        map.put("priority", priority);
        map.put("status", status);
        map.put("view_count", viewCount);
        map.put("create_time", createTime);
        map.put("update_time", updateTime);
        map.put("similarity_score", similarityScore);
        return map;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final KnowledgeDocument document;

        public Builder() {
            this.document = new KnowledgeDocument();
        }

        public Builder title(String title) {
            document.setTitle(title);
            return this;
        }

        public Builder content(String content) {
            document.setContent(content);
            return this;
        }

        public Builder summary(String summary) {
            document.setSummary(summary);
            return this;
        }

        public Builder category(String category) {
            document.setCategory(category);
            return this;
        }

        public Builder categoryId(Long categoryId) {
            document.setCategoryId(categoryId);
            return this;
        }

        public Builder tags(List<String> tags) {
            document.setTagsList(tags);
            return this;
        }

        public Builder keywords(List<String> keywords) {
            document.setKeywordsList(keywords);
            return this;
        }

        public Builder source(String source) {
            document.setSource(source);
            return this;
        }

        public Builder author(String author) {
            document.setAuthor(author);
            return this;
        }

        public Builder priority(Integer priority) {
            document.setPriority(priority);
            return this;
        }

        public Builder status(Integer status) {
            document.setStatus(status);
            return this;
        }

        public Builder createUser(Long userId, String userName) {
            document.setCreateUserId(userId);
            document.setCreateUserName(userName);
            return this;
        }

        public Builder updateUser(Long userId, String userName) {
            document.setUpdateUserId(userId);
            document.setUpdateUserName(userName);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            document.setMetadataMap(metadata);
            return this;
        }

        public KnowledgeDocument build() {
            return document;
        }
    }

}