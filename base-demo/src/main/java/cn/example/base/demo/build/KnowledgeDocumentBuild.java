package cn.example.base.demo.build;

import cn.example.base.demo.param.dto.KnowledgeDocumentDto;
import cn.example.base.demo.entity.KnowledgeDocument;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class KnowledgeDocumentBuild {

    public KnowledgeDocument buildAddKnowledgeDocument(KnowledgeDocumentDto dto){
        KnowledgeDocument document = new KnowledgeDocument();
        document.setTitle(dto.getTitle());
        document.setContent(dto.getContent());
        document.setSummary(dto.getSummary());
        document.setCategoryId(dto.getCategoryId());
        document.setSource(dto.getSource());
        document.setAuthor(dto.getAuthor());
        document.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        document.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        document.setIsVectorized(0);
        document.setCreateTime(System.currentTimeMillis());

        if (CollUtil.isNotEmpty(dto.getTags())) {
            document.setTags(String.valueOf(new Gson().toJsonTree(dto.getTags())));
        }

        if (CollUtil.isNotEmpty(dto.getKeywords())) {
            document.setKeywords(String.valueOf(new Gson().toJsonTree(dto.getKeywords())));
        }

        return document;
    }

    public boolean buildUpdateKnowledgeDocument(KnowledgeDocument document, KnowledgeDocumentDto dto){
        boolean contentChanged = false;

        contentChanged |= updateStringField(dto.getTitle(), document.getTitle(), document::setTitle);
        contentChanged |= updateStringField(dto.getContent(), document.getContent(), document::setContent);
        contentChanged |= updateStringField(dto.getSummary(), document.getSummary(), document::setSummary);
        contentChanged |= updateLongField(dto.getCategoryId(), document.getCategoryId(), document::setCategoryId);
        contentChanged |= updateTags(dto.getTags(), document);
        contentChanged |= updateKeywords(dto.getKeywords(), document);
        contentChanged |= updateStringField(dto.getSource(), document.getSource(), document::setSource);
        contentChanged |= updateIntegerField(dto.getPriority(), document.getPriority(), document::setPriority);
        contentChanged |= updateIntegerField(dto.getStatus(), document.getStatus(), document::setStatus);

        document.setIsVectorized(2);
        document.setVersion(document.getVersion() + 1);
        return contentChanged;
    }

    private boolean updateStringField(String newValue, String oldValue, java.util.function.Consumer<String> setter) {
        if (StrUtil.isNotBlank(newValue) && !newValue.equals(oldValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    private boolean updateLongField(Long newValue, Long oldValue, java.util.function.Consumer<Long> setter) {
        if (newValue != null && !newValue.equals(oldValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    private boolean updateIntegerField(Integer newValue, Integer oldValue, java.util.function.Consumer<Integer> setter) {
        if (newValue != null && !newValue.equals(oldValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    private boolean updateTags(List<String> tags, KnowledgeDocument document) {
        if (CollUtil.isNotEmpty(tags)) {
            document.setTags(String.valueOf(new Gson().toJsonTree(tags)));
            return true;
        }
        return false;
    }

    private boolean updateKeywords(List<String> keywords, KnowledgeDocument document) {
        if (CollUtil.isNotEmpty(keywords)) {
            document.setKeywords(String.valueOf(new Gson().toJsonTree(keywords)));
            return true;
        }
        return false;
    }

}
