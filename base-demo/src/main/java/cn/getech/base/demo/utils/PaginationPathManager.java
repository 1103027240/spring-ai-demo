package cn.getech.base.demo.utils;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分页路径管理器（基于Redis）
 * 
 * 用于在Redis中存储分页路径，支持无限分页
 * 避免内存无限增长，同时保证向后分页的准确性
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
    public void savePath(String pathId, CursorUtils.PaginationPath path) {
        if (pathId == null || path == null) {
            return;
        }

        try {
            String key = PATH_KEY_PREFIX + pathId;
            String encodedPath = CursorUtils.encodePaginationPath(path);
            
            if (encodedPath != null) {
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
    public CursorUtils.PaginationPath loadPath(String pathId) {
        if (pathId == null) {
            return null;
        }

        try {
            String key = PATH_KEY_PREFIX + pathId;
            String encodedPath = (String) redisTemplate.opsForValue().get(key);
            
            if (encodedPath != null) {
                CursorUtils.PaginationPath path = CursorUtils.decodePaginationPath(encodedPath);
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

    /**
     * 删除分页路径
     */
    public void deletePath(String pathId) {
        if (pathId == null) {
            return;
        }

        try {
            String key = PATH_KEY_PREFIX + pathId;
            redisTemplate.delete(key);
            log.debug("分页路径已从Redis删除，pathId: {}", pathId);
        } catch (Exception e) {
            log.error("删除分页路径失败，pathId: {}", pathId, e);
        }
    }

    /**
     * 更新分页路径的过期时间
     */
    public void refreshPathExpire(String pathId) {
        if (pathId == null) {
            return;
        }

        try {
            String key = PATH_KEY_PREFIX + pathId;
            redisTemplate.expire(key, PATH_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("分页路径过期时间已刷新，pathId: {}", pathId);
        } catch (Exception e) {
            log.error("刷新分页路径过期时间失败，pathId: {}", pathId, e);
        }
    }
}
