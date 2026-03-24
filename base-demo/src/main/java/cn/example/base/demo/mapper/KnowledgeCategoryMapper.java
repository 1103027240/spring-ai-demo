package cn.example.base.demo.mapper;

import cn.example.base.demo.entity.KnowledgeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

/**
 * 知识库分类 Mapper 接口
 */
@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {

}
