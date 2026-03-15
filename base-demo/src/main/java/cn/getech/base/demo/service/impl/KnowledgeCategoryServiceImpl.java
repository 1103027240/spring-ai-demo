package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.entity.KnowledgeCategory;
import cn.getech.base.demo.mapper.KnowledgeCategoryMapper;
import cn.getech.base.demo.service.KnowledgeCategoryService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeCategoryServiceImpl extends ServiceImpl<KnowledgeCategoryMapper, KnowledgeCategory> implements KnowledgeCategoryService {

    @Override
    public void validateCategory(String category) {
        if (StrUtil.isNotBlank(category)) {
            KnowledgeCategory categoryEntity = baseMapper.selectByName(category);
            if (categoryEntity == null) {
                throw new RuntimeException("分类不存在: " + category);
            }
        }
    }

}
