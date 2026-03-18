package cn.getech.base.demo.check;

import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.enums.CursorDirectionEnum;
import cn.getech.base.demo.enums.CursorSortByEnum;
import cn.getech.base.demo.enums.SortDirectionEnum;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import static cn.getech.base.demo.enums.CursorDirectionEnum.FIRST;
import static cn.getech.base.demo.enums.CursorSortByEnum.*;

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
            return Float.compare(Float.parseFloat(key1), Float.parseFloat(key2));
        } else if (Arrays.asList(CREATE_TIME.getId(), DOC_ID.getId()).contains(field)) {
            return Long.compare(Long.parseLong(key1), Long.parseLong(key2));
        } else {
            return key1.compareTo(key2);
        }
    }

}
