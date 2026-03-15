package cn.getech.base.demo.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import static cn.getech.base.demo.utils.CursorUtils.MAX_CURSOR_ID_COUNT;

@Data
@Getter
@Setter
@NoArgsConstructor
public class PageInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Double minScore;

    private Double maxScore;

    private Long lastId;

    private Set<Long> ids;

    private List<Long> sortedIds;  // 有序的文档ID列表（按分数降序）

    private Double lastExactScore;  // 最后一条数据的精确分数（用于精确分页）

    private Long lastExactId;       // 最后一条数据的精确ID（用于精确分页，ID是有序的）

    public PageInfoVO(Double minScore, Double maxScore, Long lastId, Set<Long> ids) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.lastId = lastId;
        this.ids = ids;
        this.sortedIds = new ArrayList<>();  // 初始化为空列表
        this.lastExactScore = null;
        this.lastExactId = null;
    }

    public PageInfoVO(Double minScore, Double maxScore, Long lastId, Set<Long> ids, List<Long> sortedIds, Double lastExactScore, Long lastExactId) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.lastId = lastId;
        this.ids = ids;
        this.sortedIds = sortedIds != null ? sortedIds : new ArrayList<>();
        this.lastExactScore = lastExactScore;
        this.lastExactId = lastExactId;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();

        // 分数只保留4位小数，减少字符串长度
        sb.append(String.format("%.4f", minScore != null ? minScore : 0.0)).append(",");
        sb.append(String.format("%.4f", maxScore != null ? maxScore : 1.0)).append(",");
        sb.append(lastId != null ? lastId : "").append(",");

        // ID列表：优先使用有序列表，如果没有则使用无序集合
        List<Long> idListToEncode;
        if (CollUtil.isNotEmpty(sortedIds)) {
            idListToEncode = sortedIds;
        } else if (CollUtil.isNotEmpty(ids)) {
            idListToEncode = new ArrayList<>(ids);
            ListUtil.sort(idListToEncode, Collections.reverseOrder()); // 排序后取最大的几个（这些是最新的）
        } else {
            idListToEncode = new ArrayList<>();
        }

        // 限制ID数量
        int limit = Math.min(idListToEncode.size(), MAX_CURSOR_ID_COUNT);
        if (limit > 0) {
            sb.append(idListToEncode.subList(0, limit).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }

        // 添加精确分数和精确ID（新格式）
        sb.append(",");
        sb.append(lastExactScore != null ? String.format("%.4f", lastExactScore) : "").append(",");
        sb.append(lastExactId != null ? lastExactId : "");

        return sb.toString();
    }

    public static PageInfoVO decode(String data) {
        String[] parts = data.split(",");
        if (parts.length < 4) {
            return new PageInfoVO(0.0d, 1.0d, null, new HashSet<>());
        }
        Double minScore = Double.parseDouble(parts[0]);
        Double maxScore = Double.parseDouble(parts[1]);
        Long lastId = StrUtil.isNotBlank(parts[2]) ? Long.parseLong(parts[2]) : null;

        Set<Long> ids = new HashSet<>();
        List<Long> sortedIds = new ArrayList<>();

        if (!parts[3].isEmpty()) {
            String[] idParts = parts[3].split(",");
            for (String idStr : idParts) {
                if (StrUtil.isNotBlank(idStr)) {
                    Long id = Long.valueOf(idStr);
                    ids.add(id);
                    if (ids.size() <= MAX_CURSOR_ID_COUNT) {
                        sortedIds.add(id);
                    }
                }
            }
        }

        // 解析精确分数和精确ID
        Double lastExactScore = null;
        Long lastExactId = null;
        if (parts.length >= 6) {
            if (StrUtil.isNotBlank(parts[4])) {
                lastExactScore = Double.parseDouble(parts[4]);
            }
            if (StrUtil.isNotBlank(parts[5])) {
                lastExactId = Long.parseLong(parts[5]);
            }
        }

        return new PageInfoVO(minScore, maxScore, lastId, ids, sortedIds, lastExactScore, lastExactId);
    }

}
