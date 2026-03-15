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

    // 移除分页路径限制，支持无限分页（使用Redis存储）
    // private static final int MAX_PAGINATION_PATH_SIZE = 50; // 已移除限制

    private static final int MAX_ACCUMULATED_IDS = 200; // 最多累积200个ID（优化内存占用）

    // 移除警告阈值
    // private static final int WARNING_PAGE_THRESHOLD = 40; // 已移除

    /**
     * 分页路径信息（用于记录完整分页历史，支持精确的 backward 分页）
     * 优化：使用滑动窗口策略，限制分页路径长度
     */
    public static class PaginationPath {
        private LinkedList<PageInfo> pageList = new LinkedList<>();  // 使用LinkedList便于删除头部
        public int currentIndex = -1;
        private int totalPageCount = 0;  // 总页数

        public void addPage(Double minScore, Double maxScore, Long lastId, Set<Long> ids) {
            // 如果当前不在列表末尾，删除后面的页面
            if (currentIndex < pageList.size() - 1) {
                while (pageList.size() > currentIndex + 1) {
                    pageList.removeLast();
                }
            }

            // 添加新页面（无限制，支持无限分页）
            PageInfo pageInfo = new PageInfo(minScore, maxScore, lastId, limitIdSet(ids));
            pageList.addLast(pageInfo);
            currentIndex = pageList.size() - 1;
            totalPageCount++;
        }

        /**
         * 直接添加PageInfo对象（保留有序ID列表）
         */
        public void addPageFromPageInfo(PageInfo pageInfo) {
            // 如果当前不在列表末尾，删除后面的页面
            if (currentIndex < pageList.size() - 1) {
                while (pageList.size() > currentIndex + 1) {
                    pageList.removeLast();
                }
            }

            // 添加新页面（无限制，支持无限分页）
            pageList.addLast(pageInfo);
            currentIndex = pageList.size() - 1;
            totalPageCount++;
        }

        /**
         * 限制ID集合大小，优化内存占用
         */
        private Set<Long> limitIdSet(Set<Long> ids) {
            if (ids.size() <= MAX_ACCUMULATED_IDS) {
                return new HashSet<>(ids);
            }
            // 保留最近的ID
            List<Long> idList = new ArrayList<>(ids);
            Collections.sort(idList, Collections.reverseOrder());  // 降序，保留最新的
            return new HashSet<>(idList.subList(0, MAX_ACCUMULATED_IDS));
        }

        public PageInfo getCurrentPage() {
            return currentIndex >= 0 && currentIndex < pageList.size() ? pageList.get(currentIndex) : null;
        }

        public PageInfo getPreviousPage() {
            return currentIndex > 0 ? pageList.get(currentIndex - 1) : null;
        }

        public PageInfo getNextPage() {
            return currentIndex >= 0 && currentIndex < pageList.size() - 1 ? pageList.get(currentIndex + 1) : null;
        }

        public boolean hasPrevious() {
            return currentIndex > 0;
        }

        public boolean hasNext() {
            return currentIndex >= 0 && currentIndex < pageList.size() - 1;
        }

        /**
         * 检查是否可以向后再翻页
         */
        public boolean canPageBackward() {
            return currentIndex > 0;
        }

        /**
         * 检查是否可以向前翻页
         */
        public boolean canPageForward() {
            return currentIndex >= 0;
        }

        /**
         * 获取最早页码（始终为1）
         */
        public int getFirstPageIndex() {
            return 1;
        }

        /**
         * 检查是否丢失了早期的页面（始终为false，因为不限制分页）
         */
        public boolean hasLostEarlyPages() {
            return false;
        }

        /**
         * 获取丢失的页面数（始终为0）
         */
        public int getLostPageCount() {
            return 0;
        }

        /**
         * 获取当前页码
         */
        public int getCurrentPageIndex() {
            return currentIndex + 1;
        }

        /**
         * 获取总页数
         */
        public int getTotalPageCount() {
            return totalPageCount;
        }

        public String encode() {
            long startTime = System.currentTimeMillis();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(currentIndex).append(CURSOR_SEPARATOR);
                sb.append(totalPageCount).append(CURSOR_SEPARATOR);  // 记录总页数

                int pageSize = pageList.size();
                for (int i = 0; i < pageSize; i++) {
                    PageInfo page = pageList.get(i);
                    sb.append(page.encode());
                    if (i < pageSize - 1) {
                        sb.append(";");
                    }
                }

                String encoded = Base64.getEncoder().encodeToString(sb.toString().getBytes());
                long duration = System.currentTimeMillis() - startTime;
                if (duration > 10) {
                    log.debug("游标编码耗时: {}ms, 路径大小: {}, 总页数: {}", duration, pageSize, totalPageCount);
                }
                return encoded;
            } catch (Exception e) {
                log.error("编码分页路径失败", e);
                return null;
            }
        }

        public static PaginationPath decode(String encoded) {
            long startTime = System.currentTimeMillis();
            try {
                if (StrUtil.isBlank(encoded)) {
                    return new PaginationPath();
                }
                byte[] decodedBytes = Base64.getDecoder().decode(encoded);
                String data = new String(decodedBytes);
                String[] parts = data.split("\\" + CURSOR_SEPARATOR, 3);
                if (parts.length < 3) {
                    return new PaginationPath();
                }
                int index = Integer.parseInt(parts[0]);
                int totalPageCount = Integer.parseInt(parts[1]);
                String[] pagesData = parts[2].split(";");

                PaginationPath path = new PaginationPath();
                path.totalPageCount = totalPageCount;

                for (String pageData : pagesData) {
                    path.pageList.addLast(PageInfo.decode(pageData));
                }
                path.currentIndex = Math.min(index, path.pageList.size() - 1);

                long duration = System.currentTimeMillis() - startTime;
                if (duration > 10) {
                    log.debug("游标解码耗时: {}ms, 路径大小: {}, 总页数: {}", duration, path.pageList.size(), totalPageCount);
                }
                return path;
            } catch (Exception e) {
                log.warn("解码分页路径失败，path: {}", encoded, e);
                return new PaginationPath();
            }
        }
    }

    /**
     * 单页信息
     * 优化：保存有序的文档ID列表（按分数降序），确保向后分页返回的数据完全一致
     */
    public static class PageInfo {
        public Double minScore;
        public Double maxScore;
        public Long lastId;
        public Set<Long> ids;
        public List<Long> sortedIds;  // 有序的文档ID列表（按分数降序）

        public PageInfo(Double minScore, Double maxScore, Long lastId, Set<Long> ids) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.lastId = lastId;
            this.ids = ids;
            this.sortedIds = new ArrayList<>();  // 初始化为空列表
        }

        public String encode() {
            // 优化：使用更紧凑的格式，减少字符串长度
            StringBuilder sb = new StringBuilder();
            // 分数只保留4位小数，减少字符串长度
            sb.append(String.format("%.4f", minScore != null ? minScore : 0.0)).append(",");
            sb.append(String.format("%.4f", maxScore != null ? maxScore : 1.0)).append(",");
            sb.append(lastId != null ? lastId : "").append(",");

            // ID列表：优先使用有序列表，如果没有则使用无序集合
            List<Long> idListToEncode;
            if (sortedIds != null && !sortedIds.isEmpty()) {
                idListToEncode = sortedIds;
            } else if (ids != null && !ids.isEmpty()) {
                idListToEncode = new ArrayList<>(ids);
                // 排序后取最大的几个（这些是最新的）
                List.sort(idListToEncode, Collections.reverseOrder());
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

            return sb.toString();
        }

        public static PageInfo decode(String data) {
            String[] parts = data.split(",");
            if (parts.length < 4) {
                return new PageInfo(0.0, 1.0, null, new HashSet<>());
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
            
            PageInfo pageInfo = new PageInfo(minScore, maxScore, lastId, ids);
            pageInfo.sortedIds = sortedIds;
            return pageInfo;
        }
    }

    /**
     * 编码游标（旧格式，用于兼容）
     * @param minSimilarityScore 最低相似度分数
     * @param maxSimilarityScore 最高相似度分数
     * @param lastId 最后一个文档的ID（混合模式）
     * @param returnedIds 已返回的文档ID集合
     * @return Base64编码的游标字符串
     */
    public static String encodeCursor(Double minSimilarityScore, Double maxSimilarityScore, Long lastId, Set<Long> returnedIds) {
        return encodeCursor(minSimilarityScore, maxSimilarityScore, lastId, returnedIds, null);
    }

    /**
     * 编码游标（新格式，支持pathId）
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
    public static String encodePaginationPath(PaginationPath path) {
        return path.encode();
    }

    /**
     * 解码分页路径游标
     */
    public static PaginationPath decodePaginationPath(String cursor) {
        return PaginationPath.decode(cursor);
    }

    /**
     * 从游标中获取之前返回的文档ID集合
     */
    public static Set<Long> getPreviousReturnedIds(String cursor) {
        Set<Long> ids = new HashSet<>();
        if (StrUtil.isBlank(cursor)) {
            return ids;
        }

        try {
            // 尝试解析为新格式（分页路径）
            if (isPaginationPathCursor(cursor)) {
                PaginationPath path = decodePaginationPath(cursor);
                PageInfo currentPage = path.getCurrentPage();
                if (currentPage != null) {
                    return new HashSet<>(currentPage.ids);
                }
            } else {
                // 兼容旧格式
                String[] cursorParts = decodeCursor(cursor);
                if (cursorParts != null && cursorParts.length >= 4) {
                    String idString = cursorParts[3];
                    if (StrUtil.isNotBlank(idString)) {
                        ids = parseIdList(idString);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取已返回ID失败，cursor: {}", cursor, e);
        }

        return ids;
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
