package cn.example.base.demo.mapper;

import cn.example.base.demo.entity.KnowledgeDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    /**
     * 根据条件查询文档列表
     */
    List<KnowledgeDocument> selectByCondition(@Param("condition") Map<String, Object> condition);

    /**
     * 根据条件统计文档数量
     */
    int countByCondition(@Param("condition") Map<String, Object> condition);

}
