package cn.getech.base.demo.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CursorUtils {

    private static final int MAX_CURSOR_ID_COUNT = 100; // 游标中最多保存的ID数量

    private static final String CURSOR_SEPARATOR = "|"; // 游标字段分隔符

    private static final String ID_SEPARATOR = ","; // ID列表分隔符

    /**
     * 编码游标（简化版）
     * 返回数据格式：minScore|lastId|id1,id2,id3,...
     */
    public static String encodeCursorSimple(Double minScore, Long lastId, Set<Long> returnedIds) {
        try {
            StringBuilder cursorData = new StringBuilder();
            cursorData.append(minScore != null ? minScore : "0.0").append(CURSOR_SEPARATOR);
            cursorData.append(lastId != null ? lastId : "").append(CURSOR_SEPARATOR);

            List<Long> idList = returnedIds.stream()
                    .skip(Math.max(0, returnedIds.size() - MAX_CURSOR_ID_COUNT))
                    .collect(Collectors.toList());

            cursorData.append(idList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(ID_SEPARATOR)));

            return Base64.getEncoder().encodeToString(cursorData.toString().getBytes());
        } catch (Exception e) {
            log.error("编码游标失败", e);
            return null;
        }
    }

    /**
     * 编码游标（完整版）
     * 返回数据格式：minScore|maxScore|lastId|id1,id2,id3,...
     * - minScore: 最低分数（forward方向使用）
     * - maxScore: 最高分数（backward方向使用）
     * - lastId: 最后一个文档的ID（混合模式下使用，普通模式为空）
     * - ids: 已返回的文档ID列表，通过逗号分隔（用于去重）
     * @param minSimilarityScore 最低相似度分数
     * @param maxSimilarityScore 最高相似度分数
     * @param lastId 最后一个文档的ID（混合模式）
     * @param returnedIds 已返回的文档ID集合
     * @return Base64编码的游标字符串
     */
    public static String encodeCursor(Double minSimilarityScore, Double maxSimilarityScore, Long lastId, Set<Long> returnedIds) {
        try {
            // 构建游标数据：minScore|maxScore|lastId|id1,id2,id3,...
            StringBuilder cursorData = new StringBuilder();
            cursorData.append(minSimilarityScore != null ? minSimilarityScore : "0.0").append(CURSOR_SEPARATOR);
            cursorData.append(maxSimilarityScore != null ? maxSimilarityScore : "1.0").append(CURSOR_SEPARATOR);
            cursorData.append(lastId != null ? lastId : "").append(CURSOR_SEPARATOR);

            // 只保留最近的MAX_CURSOR_ID_COUNT个ID（避免游标过长）
            List<Long> idList = returnedIds.stream()
                    .skip(Math.max(0, returnedIds.size() - MAX_CURSOR_ID_COUNT))
                    .collect(Collectors.toList());

            cursorData.append(idList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(ID_SEPARATOR)));

            return Base64.getEncoder().encodeToString(cursorData.toString().getBytes());
        } catch (Exception e) {
            log.error("编码游标失败", e);
            return null;
        }
    }

    /**
     * 解码游标
     * 请求参数支持两种格式：
     * - 简化版（similaritySearch使用）：minScore|lastId|id1,id2,id3,...
     * - 完整版（searchDocument使用）：minScore|maxScore|lastId|id1,id2,id3,...
     * @param cursor Base64编码的游标字符串
     * @return 解码后的数组
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
     * 从游标中获取之前返回的文档ID集合
     * 请求参数支持格式：
     * - 新格式：minScore|maxScore|lastId|id1,id2,id3,... （ids在索引3）
     * - 旧格式：minScore|id1,id2,id3,... （ids在索引1）
     * @param cursor 游标字符串
     * @return 已返回的文档ID集合
     */
    public static Set<Long> getPreviousReturnedIds(String cursor) {
        Set<Long> ids = new HashSet<>();
        if (StrUtil.isBlank(cursor)) {
            return ids;
        }

        try {
            String[] cursorParts = decodeCursor(cursor);
            if (cursorParts == null) {
                return ids;
            }

            // 优先使用新格式
            if (cursorParts.length >= 4) {
                String idString = cursorParts[3];
                if (StrUtil.isNotBlank(idString)) {
                    ids = parseIdList(idString);
                }
            }

            // 兼容旧格式
            else if (cursorParts.length > 1) {
                String idString = cursorParts[1];
                if (StrUtil.isNotBlank(idString)) {
                    ids = parseIdList(idString);
                }
            }
        } catch (Exception e) {
            log.warn("获取已返回ID失败，cursor: {}", cursor, e);
        }

        return ids;
    }

    /**
     * 解析ID列表字符串
     * @param idString ID列表字符串，如"1,2,3,4"
     * @return ID集合
     */
    public static Set<Long> parseIdList(String idString) {
        return Arrays.stream(idString.split("\\" + ID_SEPARATOR))
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

}
