package cn.getech.base.demo.utils;

import cn.getech.base.demo.dto.PaginationPathVO;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * 分页路径管理器（基于Redis）
 */
@Slf4j
@Component
public class PaginationPathManager {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PATH_KEY_PREFIX = "pagination:path:";

    private static final long PATH_EXPIRE_HOURS = 2;  // 分页路径2小时后过期

    /**
     * 生成分页路径的唯一ID
     */
    public String generatePathId() {
        return IdUtil.simpleUUID();
    }

    /**
     * 保存分页路径到Redis
     */
    public void savePath(String pathId, PaginationPathVO path) {
        if (pathId == null || path == null) {
            return;
        }

        try {
            String key = PATH_KEY_PREFIX + pathId;
            String encodedPath = CursorUtils.encodePaginationPath(path);

            if (StrUtil.isNotBlank(encodedPath)) {
                redisTemplate.opsForValue().set(key, encodedPath, PATH_EXPIRE_HOURS, TimeUnit.HOURS);
                log.debug("分页路径已保存到Redis，pathId: {}, 总页数: {}", pathId, path.getTotalPageCount());
            }
        } catch (Exception e) {
            log.error("保存分页路径到Redis失败，pathId: {}", pathId, e);
        }
    }

    /**
     * 从Redis加载分页路径
     */
    public PaginationPathVO loadPath(String pathId) {
        if (StrUtil.isBlank(pathId)) {
            return null;
        }

        try {
            String key = PATH_KEY_PREFIX + pathId;
            String encodedPath = (String) redisTemplate.opsForValue().get(key);

            if (StrUtil.isNotBlank(encodedPath)) {
                PaginationPathVO path = CursorUtils.decodePaginationPath(encodedPath);
                log.debug("从Redis加载分页路径成功，pathId: {}, 当前页数: {}", pathId, path.getTotalPageCount());
                return path;
            } else {
                log.debug("Redis中未找到分页路径，pathId: {}", pathId);
                return null;
            }
        } catch (Exception e) {
            log.error("从Redis加载分页路径失败，pathId: {}", pathId, e);
            return null;
        }
    }

}
