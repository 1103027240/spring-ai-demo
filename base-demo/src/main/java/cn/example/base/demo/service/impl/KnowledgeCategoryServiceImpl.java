package cn.example.base.demo.service.impl;

import cn.example.base.demo.entity.KnowledgeCategory;
import cn.example.base.demo.mapper.KnowledgeCategoryMapper;
import cn.example.base.demo.service.KnowledgeCategoryService;
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
