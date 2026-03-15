package cn.getech.base.demo.utils;

import cn.getech.base.demo.dto.PageInfoVO;
import cn.getech.base.demo.dto.PaginationPathVO;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CursorUtils {

    public static final int MAX_CURSOR_ID_COUNT = 100; // 游标中最多保存的ID数量

    public static final String CURSOR_SEPARATOR = "|"; // 游标字段分隔符

    private static final String ID_SEPARATOR = ","; // ID列表分隔符

    // 移除分页路径限制，支持无限分页（使用Redis存储）
    // private static final int MAX_PAGINATION_PATH_SIZE = 50; // 已移除限制

    public static final int MAX_ACCUMULATED_IDS = 200; // 最多累积200个ID（优化内存占用）

    // 移除警告阈值
    // private static final int WARNING_PAGE_THRESHOLD = 40; // 已移除

    /**
     * 编码游标（支持pathId）
     * @param minSimilarityScore 最低相似度分数
     * @param maxSimilarityScore 最高相似度分数
     * @param lastId 最后一个文档的ID（混合模式）
     * @param returnedIds 已返回的文档ID集合
     * @param pathId 分页路径ID（用于无限分页）
     * @return Base64编码的游标字符串
     */
    public static String encodeCursor(Double minSimilarityScore, Double maxSimilarityScore, Long lastId, Set<Long> returnedIds, String pathId) {
        try {
            StringBuilder cursorData = new StringBuilder();
            cursorData.append(minSimilarityScore != null ? minSimilarityScore : "0.0").append(CURSOR_SEPARATOR);
            cursorData.append(maxSimilarityScore != null ? maxSimilarityScore : "1.0").append(CURSOR_SEPARATOR);
            cursorData.append(lastId != null ? lastId : "").append(CURSOR_SEPARATOR);

            List<Long> idList = returnedIds.stream()
                    .skip(Math.max(0, returnedIds.size() - MAX_CURSOR_ID_COUNT))
                    .collect(Collectors.toList());

            cursorData.append(idList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(ID_SEPARATOR)));

            // 添加pathId（新格式）
            if (StrUtil.isNotBlank(pathId)) {
                cursorData.append(CURSOR_SEPARATOR).append(pathId);
            }

            return Base64.getEncoder().encodeToString(cursorData.toString().getBytes());
        } catch (Exception e) {
            log.error("编码游标失败", e);
            return null;
        }
    }

    /**
     * 解码游标（旧格式，用于兼容）
     */
    public static String[] decodeCursor(String cursor) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String cursorData = new String(decodedBytes);
            return cursorData.split("\\" + CURSOR_SEPARATOR);
        } catch (Exception e) {
            log.error("解码游标失败，cursor: {}", cursor, e);
            return null;
        }
    }

    /**
     * 检查游标是否是新格式（包含分页路径）
     */
    public static boolean isPaginationPathCursor(String cursor) {
        if (StrUtil.isBlank(cursor)) {
            return false;
        }
        try {
            String[] cursorParts = decodeCursor(cursor);
            if (cursorParts != null && cursorParts.length >= 2) {
                String firstPart = cursorParts[0];
                try {
                    Integer.parseInt(firstPart);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 编码分页路径游标
     */
    public static String encodePaginationPath(PaginationPathVO path) {
        return path.encode();
    }

    /**
     * 解码分页路径游标
     */
    public static PaginationPathVO decodePaginationPath(String cursor) {
        return PaginationPathVO.decode(cursor);
    }

    /**
     * 解析ID列表字符串
     */
    public static Set<Long> parseIdList(String idString) {
        return Arrays.stream(idString.split("\\" + ID_SEPARATOR))
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

}
