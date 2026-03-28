package cn.example.base.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库分类实体类
 */
@Data
@TableName("knowledge_category")
@Accessors(chain = true)
public class KnowledgeCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类编码
     */
    @TableField("category_code")
    private String categoryCode;

    /**
     * 分类路径（用于快速查询子分类）
     */
    @TableField("category_path")
    private String categoryPath;

    /**
     * 父分类ID（0表示根分类）
     */
    @TableField("parent_id")
    private Long parentId = 0L;

    /**
     * 层级（从1开始）
     */
    @TableField("level")
    private Integer level = 1;

    /**
     * 排序序号（数字越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 分类图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 分类颜色
     */
    @TableField("color")
    private String color;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 文档数量统计
     */
    @TableField("document_count")
    private Integer documentCount = 0;

    /**
     * 状态：0-禁用，1-启用，2-待审核
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 是否系统内置：0-否，1-是
     */
    @TableField("is_system")
    private Integer isSystem = 0;

    /**
     * 是否允许删除：0-否，1-是
     */
    @TableField("allow_delete")
    private Integer allowDelete = 1;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField("ext_data")
    private String extData;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted = 0;

    public KnowledgeCategory() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public KnowledgeCategory(String categoryName, Long parentId, String description) {
        this();
        this.categoryName = categoryName;
        this.parentId = parentId;
        this.description = description;
    }

    public KnowledgeCategory(String categoryName, String categoryCode, Long parentId,
                             String description, Integer sortOrder) {
        this(categoryName, parentId, description);
        this.categoryCode = categoryCode;
        this.sortOrder = sortOrder;
    }

    /**
     * 获取完整的分类路径
     */
    public String getFullPath() {
        if (categoryPath != null && !categoryPath.isEmpty()) {
            return categoryPath + "/" + categoryName;
        }
        return categoryName;
    }

    /**
     * 是否是根分类
     */
    public boolean isRootCategory() {
        return parentId == null || parentId == 0L;
    }

    /**
     * 是否是系统内置分类
     */
    public boolean isSystemCategory() {
        return isSystem != null && isSystem == 1;
    }

    /**
     * 是否允许删除
     */
    public boolean isAllowDelete() {
        return allowDelete != null && allowDelete == 1;
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
     * 增加文档数量
     */
    public void incrementDocumentCount() {
        this.documentCount = (this.documentCount == null ? 0 : this.documentCount) + 1;
    }

    /**
     * 减少文档数量
     */
    public void decrementDocumentCount() {
        this.documentCount = Math.max(0, (this.documentCount == null ? 0 : this.documentCount) - 1);
    }

}
