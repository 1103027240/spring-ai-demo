package cn.example.base.demo.mapper;

import cn.example.base.demo.entity.KnowledgeDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@CacheNamespace(flushInterval = 60000, size = 512)
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

}
