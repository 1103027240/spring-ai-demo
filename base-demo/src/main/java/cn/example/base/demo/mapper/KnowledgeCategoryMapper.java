package cn.example.base.demo.mapper;

import cn.example.base.demo.entity.KnowledgeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

/**
 * 知识库分类 Mapper 接口
 */
@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {

}
