package cn.getech.base.demo.check;

import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.enums.CursorDirectionEnum;
import cn.getech.base.demo.enums.CursorSortByEnum;
import cn.getech.base.demo.enums.SortDirectionEnum;
import org.springframework.stereotype.Component;
import static cn.getech.base.demo.enums.CursorDirectionEnum.FIRST;
import static cn.getech.base.demo.enums.CursorSortByEnum.SCORE;

@Component
public class CustomerKnowledgeCheck {

    /**
     * 验证搜索参数的合法性
     */
    public void validateSearchParams(KnowledgeDocumentSearchDto dto) {
        if (dto.getThresholdSimilarity() < 0 || dto.getThresholdSimilarity() > 1) {
            dto.setThresholdSimilarity(0.7f);
        }

        if (dto.getPageSize() == null || dto.getPageSize() <= 0) {
            dto.setPageSize(20);
        }

        if (dto.getPageSize() > 100) {
            dto.setPageSize(100);
        }

        dto.setSortDirection(SortDirectionEnum.getDefaultId(dto.getSortDirection()));

        if (!CursorSortByEnum.isValidSortField(dto.getSortField())) {
            dto.setSortField(SCORE.getId());
        }

        if (!CursorDirectionEnum.isValidCursorDirection(dto.getCursorDirection())) {
            dto.setCursorDirection(FIRST.getId());
        }
    }

    /**
     * 比较排序键值
     */
    public int compareSortKey(String key1, String key2, String field) {
        if (CursorSortByEnum.SCORE.getId().equals(field)) {
            float score1 = Float.parseFloat(key1);
            float score2 = Float.parseFloat(key2);
            return Float.compare(score1, score2);
        } else if (CursorSortByEnum.CREATE_TIME.getId().equals(field) || CursorSortByEnum.DOC_ID.getId().equals(field)) {
            long value1 = Long.parseLong(key1);
            long value2 = Long.parseLong(key2);
            return Long.compare(value1, value2);
        } else {
            return key1.compareTo(key2);
        }
    }

}
