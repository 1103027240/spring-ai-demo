package cn.getech.base.demo.mapper;

import cn.getech.base.demo.entity.KnowledgeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

/**
 * 知识库分类 Mapper 接口
 */
@Mapper
@Repository
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询分类
     */
    @Select("SELECT * FROM knowledge_category WHERE id = #{id} AND is_deleted = 0")
    KnowledgeCategory selectById(@Param("id") Long id);

    /**
     * 根据名称查询分类
     */
    @Select("SELECT * FROM knowledge_category WHERE category_name = #{categoryName} AND is_deleted = 0")
    KnowledgeCategory selectByName(@Param("categoryName") String categoryName);

    /**
     * 根据编码查询分类
     */
    @Select("SELECT * FROM knowledge_category WHERE category_code = #{categoryCode} AND is_deleted = 0")
    KnowledgeCategory selectByCode(@Param("categoryCode") String categoryCode);

    /**
     * 查询所有分类
     */
    @Select("SELECT * FROM knowledge_category WHERE is_deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<KnowledgeCategory> selectAll();

    /**
     * 分页查询分类
     */
    @Select("<script>" +
            "SELECT * FROM knowledge_category WHERE is_deleted = 0 " +
            "<if test='params.status != null'>" +
            "   AND status = #{params.status} " +
            "</if>" +
            "<if test='params.keyword != null and params.keyword != \"\"'>" +
            "   AND (category_name LIKE CONCAT('%', #{params.keyword}, '%') OR description LIKE CONCAT('%', #{params.keyword}, '%')) " +
            "</if>" +
            "ORDER BY sort_order ASC, create_time DESC" +
            "</script>")
    List<KnowledgeCategory> selectByPage(@Param("params") Map<String, Object> params);

    /**
     * 插入分类
     */
    @Insert("<script>" +
            "INSERT INTO knowledge_category (" +
            "   category_name, category_code, category_path, parent_id, level, sort_order, " +
            "   icon, color, description, document_count, status, is_system, allow_delete, " +
            "   ext_data, create_user_id, create_user_name, create_time, update_user_id, update_user_name, update_time" +
            ") VALUES (" +
            "   #{categoryName}, #{categoryCode}, #{categoryPath}, #{parentId}, #{level}, #{sortOrder}, " +
            "   #{icon}, #{color}, #{description}, #{documentCount}, #{status}, #{isSystem}, #{allowDelete}, " +
            "   #{extData}, #{createUserId}, #{createUserName}, #{createTime}, #{updateUserId}, #{updateUserName}, #{updateTime}" +
            ")" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeCategory category);

    /**
     * 更新分类
     */
    @Update("<script>" +
            "UPDATE knowledge_category SET " +
            "   category_name = #{categoryName}, " +
            "   category_code = #{categoryCode}, " +
            "   category_path = #{categoryPath}, " +
            "   parent_id = #{parentId}, " +
            "   level = #{level}, " +
            "   sort_order = #{sortOrder}, " +
            "   icon = #{icon}, " +
            "   color = #{color}, " +
            "   description = #{description}, " +
            "   document_count = #{documentCount}, " +
            "   status = #{status}, " +
            "   is_system = #{isSystem}, " +
            "   allow_delete = #{allowDelete}, " +
            "   ext_data = #{extData}, " +
            "   update_user_id = #{updateUserId}, " +
            "   update_user_name = #{updateUserName}, " +
            "   update_time = #{updateTime} " +
            "WHERE id = #{id} AND is_deleted = 0" +
            "</script>")
    int updateById(KnowledgeCategory category);

    /**
     * 逻辑删除分类
     */
    @Update("UPDATE knowledge_category SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int deleteById(@Param("id") Long id);

    // ==================== 条件查询方法 ====================

    /**
     * 根据父分类ID查询子分类
     */
    @Select("SELECT * FROM knowledge_category WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询根分类
     */
    @Select("SELECT * FROM knowledge_category WHERE (parent_id = 0 OR parent_id IS NULL) AND is_deleted = 0 ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectRootCategories();

    /**
     * 根据状态查询分类
     */
    @Select("SELECT * FROM knowledge_category WHERE status = #{status} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectByStatus(@Param("status") Integer status);

    /**
     * 根据状态和父分类ID查询分类
     */
    @Select("SELECT * FROM knowledge_category WHERE status = #{status} AND parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectByStatusAndParentId(@Param("status") Integer status, @Param("parentId") Long parentId);

    /**
     * 查询启用的分类
     */
    @Select("SELECT * FROM knowledge_category WHERE status = 1 AND is_deleted = 0 ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectEnabledCategories();

    /**
     * 根据分类编码前缀查询分类
     */
    @Select("SELECT * FROM knowledge_category WHERE category_code LIKE CONCAT(#{codePrefix}, '%') AND is_deleted = 0 ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectByCodePrefix(@Param("codePrefix") String codePrefix);

    // ==================== 统计查询方法 ====================

    /**
     * 统计分类总数
     */
    @Select("SELECT COUNT(*) FROM knowledge_category WHERE is_deleted = 0")
    int countAll();

    /**
     * 根据状态统计分类数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_category WHERE status = #{status} AND is_deleted = 0")
    int countByStatus(@Param("status") Integer status);

    /**
     * 根据父分类ID统计子分类数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_category WHERE parent_id = #{parentId} AND is_deleted = 0")
    int countByParentId(@Param("parentId") Long parentId);

    /**
     * 统计启用状态的分类数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_category WHERE status = 1 AND is_deleted = 0")
    int countEnabledCategories();

    /**
     * 统计系统内置分类数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_category WHERE is_system = 1 AND is_deleted = 0")
    int countSystemCategories();

    /**
     * 统计文档数量最多的分类
     */
    @Select("SELECT * FROM knowledge_category WHERE is_deleted = 0 ORDER BY document_count DESC LIMIT #{limit}")
    List<KnowledgeCategory> selectTopByDocumentCount(@Param("limit") Integer limit);

    // ==================== 复杂查询方法 ====================

    /**
     * 查询所有分类及其统计信息
     */
    @Select("SELECT " +
            "   c.*, " +
            "   COUNT(DISTINCT d.id) as total_documents, " +
            "   COUNT(DISTINCT CASE WHEN d.status = 1 THEN d.id END) as active_documents, " +
            "   COUNT(DISTINCT CASE WHEN d.status = 0 THEN d.id END) as inactive_documents, " +
            "   COUNT(DISTINCT CASE WHEN d.is_vectorized = 1 THEN d.id END) as vectorized_documents " +
            "FROM knowledge_category c " +
            "LEFT JOIN knowledge_document d ON d.category = c.category_name AND d.is_deleted = 0 " +
            "WHERE c.is_deleted = 0 " +
            "GROUP BY c.id, c.category_name, c.parent_id, c.sort_order " +
            "ORDER BY c.sort_order ASC")
    List<Map<String, Object>> selectAllCategoriesWithStats();

    /**
     * 查询分类树
     */
    @Select("WITH RECURSIVE category_tree AS (" +
            "   SELECT id, category_name, parent_id, level, sort_order, category_name as path " +
            "   FROM knowledge_category " +
            "   WHERE (parent_id = 0 OR parent_id IS NULL) AND is_deleted = 0 " +
            "   UNION ALL " +
            "   SELECT c.id, c.category_name, c.parent_id, c.level, c.sort_order, " +
            "          CONCAT(ct.path, ' > ', c.category_name) " +
            "   FROM knowledge_category c " +
            "   INNER JOIN category_tree ct ON c.parent_id = ct.id " +
            "   WHERE c.is_deleted = 0" +
            ") " +
            "SELECT * FROM category_tree ORDER BY path")
    List<Map<String, Object>> selectCategoryTree();

    /**
     * 查询子分类树
     */
    @Select("WITH RECURSIVE subcategory_tree AS (" +
            "   SELECT id, category_name, parent_id, level, sort_order, category_name as path " +
            "   FROM knowledge_category " +
            "   WHERE id = #{categoryId} AND is_deleted = 0 " +
            "   UNION ALL " +
            "   SELECT c.id, c.category_name, c.parent_id, c.level, c.sort_order, " +
            "          CONCAT(st.path, ' > ', c.category_name) " +
            "   FROM knowledge_category c " +
            "   INNER JOIN subcategory_tree st ON c.parent_id = st.id " +
            "   WHERE c.is_deleted = 0" +
            ") " +
            "SELECT * FROM subcategory_tree ORDER BY path")
    List<Map<String, Object>> selectSubcategoryTree(@Param("categoryId") Long categoryId);

    /**
     * 根据分类名称模糊查询
     */
    @Select("SELECT * FROM knowledge_category " +
            "WHERE category_name LIKE CONCAT('%', #{keyword}, '%') " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC " +
            "LIMIT #{limit}")
    List<KnowledgeCategory> searchByName(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 根据分类路径查询分类
     */
    @Select("SELECT * FROM knowledge_category " +
            "WHERE category_path LIKE CONCAT(#{pathPrefix}, '%') " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC")
    List<KnowledgeCategory> selectByPathPrefix(@Param("pathPrefix") String pathPrefix);

    // ==================== 业务操作方法 ====================

    /**
     * 更新分类文档数量
     */
    @Update("UPDATE knowledge_category " +
            "SET document_count = ( " +
            "   SELECT COUNT(*) FROM knowledge_document " +
            "   WHERE category = #{categoryName} AND is_deleted = 0" +
            ") " +
            "WHERE category_name = #{categoryName} AND is_deleted = 0")
    int updateDocumentCount(@Param("categoryName") String categoryName);

    /**
     * 批量更新分类状态
     */
    @Update("<script>" +
            "UPDATE knowledge_category " +
            "SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='categoryIds' item='categoryId' open='(' separator=',' close=')'>" +
            "   #{categoryId}" +
            "</foreach>" +
            "AND is_deleted = 0" +
            "</script>")
    int batchUpdateStatus(@Param("categoryIds") List<Long> categoryIds, @Param("status") Integer status);

    /**
     * 批量更新分类排序
     */
    @Update("<script>" +
            "<foreach collection='categories' item='category' separator=';'>" +
            "   UPDATE knowledge_category " +
            "   SET sort_order = #{category.sortOrder}, update_time = NOW() " +
            "   WHERE id = #{category.id} AND is_deleted = 0" +
            "</foreach>" +
            "</script>")
    int batchUpdateSortOrder(@Param("categories") List<KnowledgeCategory> categories);

    /**
     * 更新分类路径
     */
    @Update("UPDATE knowledge_category " +
            "SET category_path = #{categoryPath}, level = #{level} " +
            "WHERE id = #{id} AND is_deleted = 0")
    int updateCategoryPath(@Param("id") Long id, @Param("categoryPath") String categoryPath, @Param("level") Integer level);

    /**
     * 更新子分类的路径
     */
    @Update("UPDATE knowledge_category c " +
            "JOIN ( " +
            "   WITH RECURSIVE subcategories AS ( " +
            "       SELECT id, parent_id, category_name, 1 as depth " +
            "       FROM knowledge_category " +
            "       WHERE id = #{parentId} AND is_deleted = 0 " +
            "       UNION ALL " +
            "       SELECT c.id, c.parent_id, c.category_name, s.depth + 1 " +
            "       FROM knowledge_category c " +
            "       INNER JOIN subcategories s ON c.parent_id = s.id " +
            "       WHERE c.is_deleted = 0" +
            "   ) " +
            "   SELECT id, depth FROM subcategories" +
            ") s ON c.id = s.id " +
            "SET c.category_path = CONCAT(#{parentPath}, ' > ', c.category_name), c.level = s.depth")
    int updateSubcategoriesPath(@Param("parentId") Long parentId, @Param("parentPath") String parentPath);

    /**
     * 根据分类名称更新文档的分类名称
     */
    @Update("UPDATE knowledge_document " +
            "SET category = #{newCategoryName}, update_time = NOW() " +
            "WHERE category = #{oldCategoryName} AND is_deleted = 0")
    int updateCategoryName(@Param("oldCategoryName") String oldCategoryName, @Param("newCategoryName") String newCategoryName);

    /**
     * 检查分类名称是否已存在
     */
    @Select("SELECT COUNT(*) FROM knowledge_category " +
            "WHERE category_name = #{categoryName} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "   AND id != #{excludeId}" +
            "</if>")
    int existsByName(@Param("categoryName") String categoryName, @Param("excludeId") Long excludeId);

    /**
     * 检查分类编码是否已存在
     */
    @Select("SELECT COUNT(*) FROM knowledge_category " +
            "WHERE category_code = #{categoryCode} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "   AND id != #{excludeId}" +
            "</if>")
    int existsByCode(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);

    /**
     * 获取下一级可用的排序序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) + 1 FROM knowledge_category " +
            "WHERE parent_id = #{parentId} AND is_deleted = 0")
    Integer getNextSortOrder(@Param("parentId") Long parentId);

    // ==================== 批量操作方法 ====================

    /**
     * 批量插入分类
     */
    @Insert("<script>" +
            "INSERT INTO knowledge_category (" +
            "   category_name, category_code, parent_id, level, sort_order, " +
            "   description, status, is_system, allow_delete, " +
            "   create_user_id, create_user_name, create_time, update_time" +
            ") VALUES " +
            "<foreach collection='categories' item='category' separator=','>" +
            "   (" +
            "       #{category.categoryName}, #{category.categoryCode}, #{category.parentId}, #{category.level}, #{category.sortOrder}, " +
            "       #{category.description}, #{category.status}, #{category.isSystem}, #{category.allowDelete}, " +
            "       #{category.createUserId}, #{category.createUserName}, NOW(), NOW()" +
            "   )" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("categories") List<KnowledgeCategory> categories);

    /**
     * 批量逻辑删除分类
     */
    @Update("<script>" +
            "UPDATE knowledge_category " +
            "SET is_deleted = 1, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='categoryIds' item='categoryId' open='(' separator=',' close=')'>" +
            "   #{categoryId}" +
            "</foreach>" +
            "AND is_deleted = 0" +
            "</script>")
    int batchDelete(@Param("categoryIds") List<Long> categoryIds);

    // ==================== 系统初始化方法 ====================

    /**
     * 初始化系统内置分类
     */
    @Insert("INSERT INTO knowledge_category (" +
            "   category_name, category_code, parent_id, level, sort_order, " +
            "   description, status, is_system, allow_delete, " +
            "   create_user_id, create_user_name, create_time, update_time" +
            ") VALUES " +
            "   ('常见问题', 'FAQ', 0, 1, 1, '系统常见问题分类', 1, 1, 0, 0, 'system', NOW(), NOW()), " +
            "   ('知识库', 'KNOWLEDGE_BASE', 0, 1, 2, '系统知识库分类', 1, 1, 0, 0, 'system', NOW(), NOW()), " +
            "   ('政策法规', 'POLICY', 0, 1, 3, '政策法规分类', 1, 1, 0, 0, 'system', NOW(), NOW()), " +
            "   ('使用指南', 'USER_GUIDE', 0, 1, 4, '用户使用指南', 1, 1, 0, 0, 'system', NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "   update_time = VALUES(update_time)")
    int initSystemCategories();
}
