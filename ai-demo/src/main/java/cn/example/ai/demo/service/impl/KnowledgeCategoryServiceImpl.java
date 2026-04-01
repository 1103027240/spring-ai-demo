package cn.example.ai.demo.service.impl;

import cn.example.ai.demo.entity.KnowledgeCategory;
import cn.example.ai.demo.mapper.KnowledgeCategoryMapper;
import cn.example.ai.demo.service.KnowledgeCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class KnowledgeCategoryServiceImpl extends ServiceImpl<KnowledgeCategoryMapper, KnowledgeCategory> implements KnowledgeCategoryService {

    @Override
    public void validateCategoryId(Long categoryId) {
        if (Objects.nonNull(categoryId)) {
            KnowledgeCategory categoryEntity = baseMapper.selectById(categoryId);
            if (categoryEntity == null) {
                throw new RuntimeException("分类ID不存在: " + categoryId);
            }
        }
    }

}
