package cn.getech.base.demo.dto;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.*;
import static cn.getech.base.demo.utils.CursorUtils.CURSOR_SEPARATOR;
import static cn.getech.base.demo.utils.CursorUtils.MAX_ACCUMULATED_IDS;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationPathVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LinkedList<PageInfoVO> pageList = new LinkedList<>();  // 使用LinkedList便于删除头部

    private int currentIndex = -1;

    private int totalPageCount = 0;  // 总页数

    /**
     * 直接添加PageInfo对象（保留有序ID列表）
     */
    public void addPageFromPageInfo(PageInfoVO pageInfo) {
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
    public Set<Long> limitIdSet(Set<Long> ids) {
        if (ids.size() <= MAX_ACCUMULATED_IDS) {
            return new HashSet<>(ids);
        }
        // 保留最近的ID
        List<Long> idList = new ArrayList<>(ids);
        Collections.sort(idList, Collections.reverseOrder());  // 降序，保留最新的
        return new HashSet<>(idList.subList(0, MAX_ACCUMULATED_IDS));
    }

    public PageInfoVO getCurrentPage() {
        return currentIndex >= 0 && currentIndex < pageList.size() ? pageList.get(currentIndex) : null;
    }

    public PageInfoVO getPreviousPage() {
        return currentIndex > 0 ? pageList.get(currentIndex - 1) : null;
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    public boolean hasNext() {
        return currentIndex >= 0 && currentIndex < pageList.size() - 1;
    }

    /**
     * 获取最早页码（始终为1）
     */
    public int getFirstPageIndex() {
        return 1;
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
                PageInfoVO page = pageList.get(i);
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

    public static PaginationPathVO decode(String encoded) {
        long startTime = System.currentTimeMillis();
        try {
            if (StrUtil.isBlank(encoded)) {
                return new PaginationPathVO();
            }
            byte[] decodedBytes = Base64.getDecoder().decode(encoded);
            String data = new String(decodedBytes);
            String[] parts = data.split("\\" + CURSOR_SEPARATOR, 3);
            if (parts.length < 3) {
                return new PaginationPathVO();
            }
            int index = Integer.parseInt(parts[0]);
            int totalPageCount = Integer.parseInt(parts[1]);
            String[] pagesData = parts[2].split(";");

            PaginationPathVO path = new PaginationPathVO();
            path.totalPageCount = totalPageCount;

            for (String pageData : pagesData) {
                path.pageList.addLast(PageInfoVO.decode(pageData));
            }
            path.currentIndex = Math.min(index, path.pageList.size() - 1);

            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                log.debug("游标解码耗时: {}ms, 路径大小: {}, 总页数: {}", duration, path.pageList.size(), totalPageCount);
            }
            return path;
        } catch (Exception e) {
            log.warn("解码分页路径失败，path: {}", encoded, e);
            return new PaginationPathVO();
        }
    }

}
