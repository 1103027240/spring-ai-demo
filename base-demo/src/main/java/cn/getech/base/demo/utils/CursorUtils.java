package cn.getech.base.demo.utils;

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

    public static final int MAX_ACCUMULATED_IDS = 200; // 最多累积200个ID（优化内存占用）

    /**
     * 编码游标（支持pathId，添加精确分数和ID）
     * @param minSimilarityScore 最低相似度分数
     * @param maxSimilarityScore 最高相似度分数
     * @param lastId 最后一个文档的ID（混合模式）
     * @param returnedIds 已返回的文档ID集合
     * @param pathId 分页路径ID（用于无限分页）
     * @param lastExactScore 最后一条数据的精确分数
     * @param lastExactId 最后一条数据的精确ID
     * @return Base64编码的游标字符串
     * 返回数据格式：minSimilarityScore|maxSimilarityScore|lastId|returnedIds|pathId|lastExactScore|lastExactId
     */
    public static String encodeCursor(Double minSimilarityScore, Double maxSimilarityScore, Long lastId, Set<Long> returnedIds, String pathId, Double lastExactScore, Long lastExactId) {
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

            if (StrUtil.isNotBlank(pathId)) {
                cursorData.append(CURSOR_SEPARATOR).append(pathId);
            }

            // 添加精确分数和精确ID
            cursorData.append(CURSOR_SEPARATOR);
            cursorData.append(lastExactScore != null ? lastExactScore : "").append(CURSOR_SEPARATOR);
            cursorData.append(lastExactId != null ? lastExactId : "");

            return Base64.getEncoder().encodeToString(cursorData.toString().getBytes());
        } catch (Exception e) {
            log.error("编码游标失败", e);
            return null;
        }
    }

    /**
     * 解码游标
     * 请求数据格式：minSimilarityScore|maxSimilarityScore|lastId|returnedIds|pathId|lastExactScore|lastExactId
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
     * 编码分页路径游标
     * 返回数据格式：currentIndex|totalPageCount|pageInfo1;pageInfo2;pageInfo2...
     */
    public static String encodePaginationPath(PaginationPathVO path) {
        return path.encode();
    }

    /**
     * 解码分页路径游标
     * 请求数据格式：currentIndex|totalPageCount|pageInfo1;pageInfo2;pageInfo2...
     */
    public static PaginationPathVO decodePaginationPath(String cursor) {
        return PaginationPathVO.decode(cursor);
    }

}
