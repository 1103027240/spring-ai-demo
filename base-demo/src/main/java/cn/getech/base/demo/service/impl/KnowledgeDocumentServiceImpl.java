package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.CursorSearchResultVO;
import cn.getech.base.demo.dto.KnowledgeDocumentDto;
import cn.getech.base.demo.dto.KnowledgeDocumentSearchDto;
import cn.getech.base.demo.entity.KnowledgeCategory;
import cn.getech.base.demo.entity.KnowledgeDocument;
import cn.getech.base.demo.enums.KnowledgeDocumentStatusEnum;
import cn.getech.base.demo.enums.SearchModeEnum;
import cn.getech.base.demo.mapper.KnowledgeCategoryMapper;
import cn.getech.base.demo.mapper.KnowledgeDocumentMapper;
import cn.getech.base.demo.service.KnowledgeCategoryService;
import cn.getech.base.demo.service.KnowledgeDocumentService;
import cn.getech.base.demo.utils.CursorUtils;
import cn.getech.base.demo.utils.ObjectMapperUtils;
import cn.getech.base.demo.utils.ParamUtils;
import cn.getech.base.demo.utils.PaginationPathManager;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static cn.getech.base.demo.constant.FieldConstant.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> implements KnowledgeDocumentService {

    @Value("${app.knowledge.similarity-threshold:0.7}")
    private double similarityThreshold;

    @Resource(name = "customerKnowledgeVectorStore")
    private VectorStore customerKnowledgeVectorStore;

    @Autowired
    private KnowledgeCategoryService knowledgeCategoryService;

    @Autowired
    private ObjectMapperUtils objectMapperUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PaginationPathManager paginationPathManager;

    private static final String CACHE_PREFIX = "ai_customer:knowledge:";

    private static final int CACHE_TTL = 3600; // 1小时

    private static final int VECTOR_SEARCH_BATCH_SIZE = 100; // 向量搜索每次取100条

    private static final String CURSOR_SEPARATOR = "|"; // 游标字段分隔符

    private static final String ID_SEPARATOR = ","; // ID列表分隔符

    private static final int MAX_CURSOR_ID_COUNT = 100; // 游标中最多保存的ID数量

    private static final String CURSOR_DIRECTION_FORWARD = "forward";

    private static final String CURSOR_DIRECTION_BACKWARD = "backward";

    private static final String CURSOR_DIRECTION_FIRST = "first";

    private static final double SCORE_SAME_THRESHOLD = 0.0001; // 分数相同判定阈值

    private static final int LARGE_RESULT_THRESHOLD = 200; // 大结果集阈值，超过此值切换到混合模式

    /**
     * 搜索相关知识
     */
    @Override
    public List<Map<String, Object>> searchKnowledge(String query, int limit) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(limit)
                    .similarityThreshold(similarityThreshold)
                    .build();

            List<Document> documents = customerKnowledgeVectorStore.similaritySearch(request);

            return documents.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>(doc.getMetadata());
                        result.put(CONTENT, doc.getText());
                        return result;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("向量搜索失败，尝试文本搜索", e);
            return searchByText(query);
        }
    }

    @Transactional
    @Override
    public KnowledgeDocument createDocument(KnowledgeDocumentDto dto) {
        // 1. 验证分类是否存在
        if (StrUtil.isNotBlank(dto.getCategory())) {
            knowledgeCategoryService.validateCategory(dto.getCategory());
        }

        KnowledgeDocument document = new KnowledgeDocument();
        document.setTitle(dto.getTitle());
        document.setContent(dto.getContent());
        document.setSummary(dto.getSummary());
        document.setCategory(dto.getCategory());
        document.setSource(dto.getSource());
        document.setAuthor(dto.getAuthor());
        document.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        document.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            document.setTags(objectMapperUtils.convertToJson(dto.getTags()));
        }

        if (dto.getKeywords() != null && !dto.getKeywords().isEmpty()) {
            document.setKeywords(objectMapperUtils.convertToJson(dto.getKeywords()));
        }

        if (dto.getMetadata() != null) {
            document.setMetadata(objectMapperUtils.convertToJson(dto.getMetadata()));
        }

        // 2. 保存文档
        baseMapper.insert(document);

        // 3. 文档向量化
        vectorizeDocumentAsync(document);

        // 4. 更新分类
        if (StrUtil.isNotBlank(dto.getCategory())) {
            //knowledgeCategoryService.updateCategoryDocumentCount(document.getCategory());
        }

        // 5. 清理相关缓存
        clearKnowledgeCache(dto.getCategory());

        return document;
    }

    @Transactional
    @Override
    public void batchCreateDocuments(List<KnowledgeDocumentDto> list) {
        List<String> categories = new ArrayList<>();

        // 更新文档
        for (KnowledgeDocumentDto dto : list) {
            try {
                KnowledgeDocument document = createDocument(dto);
                if (StrUtil.isNotBlank(document.getCategory())) {
                    categories.add(document.getCategory());
                }
            } catch (Exception e) {
                log.error("批量创建文档失败，标题: {}", dto.getTitle(), e);
                // 继续处理其他文档
            }
        }

        // 更新分类
        categories.forEach(this::updateCategoryDocumentCount);
    }

    @Transactional
    @Override
    public void updateDocument(KnowledgeDocumentDto dto) {
        // 1. 获取文档
        if (dto.getId() == null) {
            throw new RuntimeException("文档ID不能为空");
        }

        KnowledgeDocument document = baseMapper.selectById(dto.getId());
        if (document == null) {
            throw new RuntimeException("文档不存在，ID: " + dto.getId());
        }

        String oldCategory = document.getCategory();

        boolean contentChanged = false;
        if (StrUtil.isNotBlank(dto.getTitle()) && !dto.getTitle().equals(document.getTitle())) {
            document.setTitle(dto.getTitle());
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getContent()) && !dto.getContent().equals(document.getContent())) {
            document.setContent(dto.getContent());
            contentChanged = true;
        }

        if (dto.getSummary() != null) {
            document.setSummary(dto.getSummary());
        }

        if (dto.getCategory() != null && !dto.getCategory().equals(document.getCategory())) {
            document.setCategory(dto.getCategory());
            contentChanged = true;
        }

        if (dto.getTags() != null) {
            document.setTags(objectMapperUtils.convertToJson(dto.getTags()));
            contentChanged = true;
        }

        if (dto.getKeywords() != null) {
            document.setKeywords(objectMapperUtils.convertToJson(dto.getKeywords()));
            contentChanged = true;
        }

        if (dto.getPriority() != null) {
            document.setPriority(dto.getPriority());
        }

        if (dto.getStatus() != null) {
            document.setStatus(dto.getStatus());
        }

        document.setVersion(document.getVersion() + 1);
        document.setUpdateTime(LocalDateTime.now());

        // 2. 更新文档
        baseMapper.updateById(document);

        // 3. 如果内容有变化，重新向量化
        if (contentChanged) {
            document.setIsVectorized(0); // 标记为未向量化
            vectorizeDocumentAsync(document);
        }

        // 4. 清理缓存
        clearDocumentCache(document.getId());
        clearKnowledgeCache(oldCategory);
        clearKnowledgeCache(document.getCategory());
    }

    @Transactional
    @Override
    public void deleteDocument(Long documentId) {
        // 1. 获取文档
        KnowledgeDocument document = baseMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在，ID: " + documentId);
        }

        // 2. 逻辑删除
        document.setStatus(KnowledgeDocumentStatusEnum.DELETED.getId()); // 已删除
        document.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(document);

        // 3. 异步删除向量
        deleteDocumentVectorAsync(document);

        // 4. 更新分类统计
        if (StrUtil.isNotBlank(document.getCategory())) {
            updateCategoryDocumentCount(document.getCategory());
        }

        // 5. 清理缓存
        clearDocumentCache(documentId);
        clearKnowledgeCache(document.getCategory());
    }

    @Override
    public KnowledgeDocument getDocumentById(Long documentId) {
        // 先从缓存获取
        String cacheKey = CACHE_PREFIX + "document:" + documentId;
        KnowledgeDocument cachedDocument = (KnowledgeDocument) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDocument != null) {
            log.debug("从缓存获取文档，ID: {}", documentId);
            return cachedDocument;
        }

        // 再从数据库获取
        KnowledgeDocument document = baseMapper.selectById(documentId);
        if (document != null && document.getStatus() != KnowledgeDocumentStatusEnum.DELETED.getId()) { // 非删除状态
            document.setViewCount(document.getViewCount() + 1);
            //knowledgeDocumentMapper.updateViewCount(documentId, document.getViewCount());

            // 最后缓存
            redisTemplate.opsForValue().set(cacheKey, document, CACHE_TTL, TimeUnit.SECONDS);
        }
        return document;
    }

    @Override
    public Map<String, Object> searchDocument(KnowledgeDocumentSearchDto dto) {
        SearchModeEnum searchModeEnum = SearchModeEnum.valueOf(dto.getSearchMode());
        String cacheKey = buildSearchCacheKey(dto);

        if (!SearchModeEnum.VECTOR.equals(searchModeEnum) && StrUtil.isBlank(dto.getCursor())) {
            Map<String, Object> cacheMap = getKeywordFromCache(cacheKey);
            if (CollUtil.isNotEmpty(cacheMap)) {
                return cacheMap;
            }
        }

        Map<String, Object> result = new HashMap<>();
        List<KnowledgeDocument> documents;

        try {
            switch (searchModeEnum) {
                case VECTOR:
                    documents = searchWithVectorMode(dto, result);
                    break;
                case KEYWORD:
                    documents = searchWithKeywordMode(dto, result);
                    break;
                case HYBRID:
                default:
                    if (StrUtil.isNotBlank(dto.getCursor())) {
                        documents = searchWithVectorMode(dto, result);
                    } else {
                        documents = searchWithHybridMode(dto, result);
                    }
                    break;
            }

            // 构建通用响应（VECTOR模式已经在searchWithVectorMode中构建，这里跳过）
            if (!SearchModeEnum.VECTOR.equals(searchModeEnum)) {
                buildCommonResponse(result, documents, dto);
            } else {
                // VECTOR模式：添加current_page和page_size
                result.put("current_page", dto.getPage());
                result.put("page_size", dto.getSize());
            }

            // 缓存结果（vector和带游标的查询不缓存）
            if (!SearchModeEnum.VECTOR.equals(searchModeEnum) && dto.getCursor() == null) {
                cacheResult(cacheKey, result);
            }
        } catch (Exception e) {
            log.error("搜索文档失败，searchMode: {}, 参数: {}", dto.getSearchMode(), dto, e);
            result.put("error", "搜索失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 向量搜索模式（游标分页）
     * 优化：改进 ID 累积策略，避免无限增长
     * 支持无限分页（使用Redis存储分页路径）
     * 
     * 容错处理：
     * - 如果Redis中找不到path或path为空，自动降级为首次查询
     * - 确保即使pathId过期或传错，也能正常显示数据
     */
    private List<KnowledgeDocument> searchWithVectorMode(KnowledgeDocumentSearchDto dto, Map<String, Object> result) {
        int limit = dto.getSize();
        String cursorDirection = dto.getCursorDirection() != null ? dto.getCursorDirection() : CURSOR_DIRECTION_FORWARD;
        long startTime = System.currentTimeMillis();

        // 管理分页路径ID
        String pathId = dto.getPathId();
        boolean pathExpired = false;  // 标记path是否过期
        
        if (StrUtil.isBlank(pathId)) {
            pathId = paginationPathManager.generatePathId();
        }

        // 从Redis加载分页路径
        CursorUtils.PaginationPath path = paginationPathManager.loadPath(pathId);
        if (path == null || path.getCurrentPage() == null) {
            // 如果Redis中找不到path或path为空，生成新的pathId并重新查询
            log.info("分页路径不存在或已过期，pathId: {}，重新开始查询", pathId);
            pathExpired = true;
            pathId = paginationPathManager.generatePathId();
            path = new CursorUtils.PaginationPath();
            cursorDirection = CURSOR_DIRECTION_FIRST;  // 强制降级为首次查询
        }

        // 根据分页方向处理
        CursorUtils.PageInfo currentPageInfo = path.getCurrentPage();
        List<KnowledgeDocument> documents;

        if (pathExpired) {
            // path过期：重新查询第一页
            CursorSearchResultVO cursorResult = vectorSearch(dto);
            documents = cursorResult.getDocuments();

            // 添加到分页路径（保存有序的ID列表）
            Set<Long> currentIds = documents.stream()
                    .map(KnowledgeDocument::getId)
                    .collect(Collectors.toSet());
            
            // 创建有序的ID列表（按分数降序）
            List<Long> sortedIds = documents.stream()
                    .sorted((a, b) -> {
                        Double s1 = a.getSimilarityScore();
                        Double s2 = b.getSimilarityScore();
                        if (s1 == null && s2 == null) return a.getId().compareTo(b.getId());
                        if (s1 == null) return 1;
                        if (s2 == null) return -1;
                        int cmp = s2.compareTo(s1);  // 分数降序
                        return cmp != 0 ? cmp : a.getId().compareTo(b.getId());
                    })
                    .map(KnowledgeDocument::getId)
                    .collect(Collectors.toList());
            
            CursorUtils.PageInfo pageInfo = new CursorUtils.PageInfo(
                    cursorResult.getMinScore(), 
                    cursorResult.getMaxScore(), 
                    cursorResult.getLastId(), 
                    currentIds);
            pageInfo.sortedIds = sortedIds;
            
            // 手动添加到分页路径
            path.addPageFromPageInfo(pageInfo);

            result.put("next_cursor", documents.size() >= limit ? CursorUtils.encodePaginationPath(path) : null);
            result.put("previous_cursor", null);
            result.put("has_next", documents.size() >= limit);
            result.put("has_previous", false);

            // 保存分页路径到Redis
            paginationPathManager.savePath(pathId, path);

            // 添加分页状态信息
            addPaginationStatus(result, path);

            log.info("路径已过期，重新查询第一页，耗时: {}ms，返回文档数: {}", System.currentTimeMillis() - startTime, documents.size());
        } else if (CURSOR_DIRECTION_BACKWARD.equals(cursorDirection) && path.hasPrevious()) {
            // Backward：返回上一页（从路径中获取）
            CursorUtils.PageInfo prevPage = path.getPreviousPage();
            path.currentIndex--;  // 移动到上一页

            documents = fetchDocumentsByPageInfo(prevPage, dto);

            // 判断has_next：是否有下一页（即能否再向前翻回）
            boolean hasNext = path.hasNext();

            result.put("next_cursor", hasNext ? CursorUtils.encodePaginationPath(path) : null);
            result.put("previous_cursor", path.hasPrevious() ? CursorUtils.encodePaginationPath(path) : null);
            result.put("has_next", hasNext);
            result.put("has_previous", path.hasPrevious());

            // 保存分页路径到Redis
            paginationPathManager.savePath(pathId, path);

            // 添加分页状态信息
            addPaginationStatus(result, path);

            log.debug("Backward分页完成，耗时: {}ms，返回文档数: {}，has_next: {}", System.currentTimeMillis() - startTime, documents.size(), hasNext);
        } else if (CURSOR_DIRECTION_FIRST.equals(cursorDirection)) {
            // First：从向量库获取第一页
            CursorSearchResultVO cursorResult = vectorSearch(dto);
            documents = cursorResult.getDocuments();

            // 添加到分页路径（保存有序的ID列表）
            Set<Long> currentIds = documents.stream()
                    .map(KnowledgeDocument::getId)
                    .collect(Collectors.toSet());
            
            // 创建有序的ID列表（按分数降序）
            List<Long> sortedIds = documents.stream()
                    .sorted((a, b) -> {
                        Double s1 = a.getSimilarityScore();
                        Double s2 = b.getSimilarityScore();
                        if (s1 == null && s2 == null) return a.getId().compareTo(b.getId());
                        if (s1 == null) return 1;
                        if (s2 == null) return -1;
                        int cmp = s2.compareTo(s1);  // 分数降序
                        return cmp != 0 ? cmp : a.getId().compareTo(b.getId());
                    })
                    .map(KnowledgeDocument::getId)
                    .collect(Collectors.toList());
            
            CursorUtils.PageInfo pageInfo = new CursorUtils.PageInfo(
                    cursorResult.getMinScore(), 
                    cursorResult.getMaxScore(), 
                    cursorResult.getLastId(), 
                    currentIds);
            pageInfo.sortedIds = sortedIds;
            
            // 手动添加到分页路径
            path.addPageFromPageInfo(pageInfo);

            result.put("next_cursor", documents.size() >= limit ? CursorUtils.encodePaginationPath(path) : null);
            result.put("previous_cursor", null);
            result.put("has_next", documents.size() >= limit);
            result.put("has_previous", false);

            // 保存分页路径到Redis
            paginationPathManager.savePath(pathId, path);

            // 添加分页状态信息
            addPaginationStatus(result, path);

            log.debug("First查询完成，耗时: {}ms，返回文档数: {}", System.currentTimeMillis() - startTime, documents.size());
        } else {
            // Forward：获取下一页
            CursorSearchResultVO cursorResult = vectorSearch(dto);
            documents = cursorResult.getDocuments();

            // 添加到分页路径（保存有序的ID列表）
            Set<Long> currentIds = documents.stream()
                    .map(KnowledgeDocument::getId)
                    .collect(Collectors.toSet());

            // 只保留最近2页的ID用于去重（优化内存占用）
            Set<Long> allIds = new HashSet<>(currentIds);
            if (currentPageInfo != null && currentPageInfo.ids != null) {
                Set<Long> previousIds = new HashSet<>(currentPageInfo.ids);
                if (previousIds.size() > 100) {
                    List<Long> idList = new ArrayList<>(previousIds);
                    Collections.sort(idList, Collections.reverseOrder());
                    previousIds = new HashSet<>(idList.subList(0, 100));
                }
                allIds.addAll(previousIds);
            }

            // 创建有序的ID列表（按分数降序）
            List<Long> sortedIds = documents.stream()
                    .sorted((a, b) -> {
                        Double s1 = a.getSimilarityScore();
                        Double s2 = b.getSimilarityScore();
                        if (s1 == null && s2 == null) return a.getId().compareTo(b.getId());
                        if (s1 == null) return 1;
                        if (s2 == null) return -1;
                        int cmp = s2.compareTo(s1);  // 分数降序
                        return cmp != 0 ? cmp : a.getId().compareTo(b.getId());
                    })
                    .map(KnowledgeDocument::getId)
                    .collect(Collectors.toList());
            
            CursorUtils.PageInfo pageInfo = new CursorUtils.PageInfo(
                    cursorResult.getMinScore(), 
                    cursorResult.getMaxScore(), 
                    cursorResult.getLastId(), 
                    allIds);
            pageInfo.sortedIds = sortedIds;
            
            // 手动添加到分页路径
            path.addPageFromPageInfo(pageInfo);

            result.put("next_cursor", documents.size() >= limit ? CursorUtils.encodePaginationPath(path) : null);
            result.put("previous_cursor", path.hasPrevious() ? CursorUtils.encodePaginationPath(path) : null);
            result.put("has_next", documents.size() >= limit);
            result.put("has_previous", path.hasPrevious());

            // 保存分页路径到Redis
            paginationPathManager.savePath(pathId, path);

            // 添加分页状态信息
            addPaginationStatus(result, path);

            log.debug("Forward分页完成，耗时: {}ms，返回文档数: {}，累积ID数: {}", System.currentTimeMillis() - startTime, documents.size(), allIds.size());
        }

        // 返回pathId给前端
        result.put("path_id", pathId);

        return documents;
    }

    /**
     * 添加分页状态信息到结果中
     */
    private void addPaginationStatus(Map<String, Object> result, CursorUtils.PaginationPath path) {
        if (path == null) {
            return;
        }

        // 添加分页状态信息
        Map<String, Object> paginationStatus = new HashMap<>();
        paginationStatus.put("current_page", path.getCurrentPageIndex());  // 当前页码
        paginationStatus.put("total_pages", path.getTotalPageCount());  // 总页数
        paginationStatus.put("earliest_available_page", path.getFirstPageIndex());  // 可回溯的最早页码（始终为1）

        result.put("pagination_status", paginationStatus);
    }

    /**
     * 根据分页信息获取文档（优化版）
     * 
     * 关键优化：
     * 1. 向后分页时，优先使用保存的有序ID列表查询数据库，确保返回的数据完全一致
     * 2. 只有在有序ID列表为空时，才重新查询向量库
     * 3. 大幅提升向后分页性能（从几百毫秒降低到几十毫秒）
     */
    private List<KnowledgeDocument> fetchDocumentsByPageInfo(CursorUtils.PageInfo pageInfo, KnowledgeDocumentSearchDto dto) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            int limit = dto.getSize();
            List<Long> sortedIds = pageInfo.sortedIds;
            Set<Long> returnedIds = pageInfo.ids;
            
            // 优先使用有序的ID列表查询数据库（确保向后分页返回的数据完全一致）
            if (sortedIds != null && !sortedIds.isEmpty()) {
                log.debug("使用保存的有序ID列表查询文档，ID数量: {}", sortedIds.size());
                
                // 批量查询文档
                int batchSize = 50;
                for (int i = 0; i < sortedIds.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, sortedIds.size());
                    List<Long> batchIds = sortedIds.subList(i, endIndex);
                    List<KnowledgeDocument> batchDocs = baseMapper.selectBatchIds(batchIds);
                    
                    for (KnowledgeDocument document : batchDocs) {
                        if (document == null || document.getStatus() != KnowledgeDocumentStatusEnum.ENABLED.getId()) {
                            continue;
                        }
                        
                        if (StrUtil.isNotBlank(dto.getCategory()) && !dto.getCategory().equals(document.getCategory())) {
                            continue;
                        }
                        
                        // 如果有分数范围，设置相似度分数
                        if (pageInfo.minScore != null && pageInfo.maxScore != null) {
                            // 使用分数范围的中间值作为近似分数
                            document.setSimilarityScore((pageInfo.minScore + pageInfo.maxScore) / 2);
                        }
                        
                        documents.add(document);
                        
                        if (documents.size() >= limit) {
                            break;
                        }
                    }
                    
                    if (documents.size() >= limit) {
                        break;
                    }
                }
                
                // 限制返回数量
                if (documents.size() > limit) {
                    documents = documents.subList(0, limit);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.debug("fetchDocumentsByPageInfo完成（使用有序ID列表），耗时: {}ms，返回文档数: {}", 
                        duration, documents.size());
                return documents;
            }
            
            // 如果有序ID列表为空，使用无序ID列表查询数据库
            if (returnedIds != null && !returnedIds.isEmpty()) {
                log.debug("使用保存的无序ID列表查询文档，ID数量: {}", returnedIds.size());
                return fetchDocumentsByIdList(returnedIds, dto, limit);
            }
            
            // 如果ID列表为空，尝试重新查询向量库
            log.debug("ID列表为空，尝试重新查询向量库");
            return fetchDocumentsByVectorSearch(pageInfo, dto);
            
        } catch (Exception e) {
            log.error("根据分页信息获取文档失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID列表查询文档（备用方案）
     */
    private List<KnowledgeDocument> fetchDocumentsByIdList(Set<Long> ids, KnowledgeDocumentSearchDto dto, int limit) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return documents;
        }

        try {
            // 转换为列表并排序
            List<Long> sortedIds = new ArrayList<>(ids);
            Collections.sort(sortedIds);
            
            // 批量查询文档
            int batchSize = 50;
            for (int i = 0; i < sortedIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, sortedIds.size());
                List<Long> batchIds = sortedIds.subList(i, endIndex);
                List<KnowledgeDocument> batchDocs = baseMapper.selectBatchIds(batchIds);
                
                for (KnowledgeDocument document : batchDocs) {
                    if (document == null || document.getStatus() != KnowledgeDocumentStatusEnum.ENABLED.getId()) {
                        continue;
                    }
                    
                    if (StrUtil.isNotBlank(dto.getCategory()) && !dto.getCategory().equals(document.getCategory())) {
                        continue;
                    }
                    
                    documents.add(document);
                    
                    if (documents.size() >= limit) {
                        break;
                    }
                }
                
                if (documents.size() >= limit) {
                    break;
                }
            }
            
            // 限制返回数量
            if (documents.size() > limit) {
                documents = documents.subList(0, limit);
            }

        } catch (Exception e) {
            log.error("根据ID列表查询文档失败", e);
        }

        return documents;
    }

    /**
     * 添加分页状态信息到结果中
     */
    private void addPaginationStatus(Map<String, Object> result, CursorUtils.PaginationPath path) {
        if (path == null) {
            return;
        }

        // 添加分页状态信息
        Map<String, Object> paginationStatus = new HashMap<>();
        paginationStatus.put("current_page", path.getCurrentPageIndex());  // 当前页码
        paginationStatus.put("total_pages", path.getTotalPageCount());  // 总页数
        paginationStatus.put("earliest_available_page", path.getFirstPageIndex());  // 可回溯的最早页码（始终为1）

        result.put("pagination_status", paginationStatus);
    }

    /**
     * 通过向量搜索获取文档（最后备用方案）
     */
    private List<KnowledgeDocument> fetchDocumentsByVectorSearch(CursorUtils.PageInfo pageInfo, KnowledgeDocumentSearchDto dto) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        int vectorQueryCount = 0;
        int filteredCount = 0;

        try {
            int limit = dto.getSize();
            Double minScore = pageInfo.minScore;
            Double maxScore = pageInfo.maxScore;
            Long lastId = pageInfo.lastId;
            Set<Long> returnedIds = pageInfo.ids;

            if (minScore == null || maxScore == null) {
                log.debug("分数范围不完整，无法查询向量库");
                return documents;
            }

            // 向量搜索
            Double scoreRange = maxScore - minScore;
            int topK = 200;
            if (scoreRange < 0.01) {
                topK = 300;
            } else if (scoreRange > 0.2) {
                topK = 100;
            }

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(dto.getKeyword())
                    .topK(topK)
                    .similarityThreshold(Math.max(0.6, minScore - 0.1))
                    .build();

            List<Document> vectorDocs = customerKnowledgeVectorStore.similaritySearch(searchRequest);
            vectorQueryCount = vectorDocs.size();

            if (CollUtil.isEmpty(vectorDocs)) {
                log.debug("向量搜索结果为空");
                return documents;
            }

            // 过滤文档
            List<Long> filteredIds = new ArrayList<>();
            for (Document doc : vectorDocs) {
                Map<String, Object> metadata = doc.getMetadata();
                Long documentId = ParamUtils.parseDocumentId(metadata.get("documentId"));
                Double score = (Double) metadata.get("similarity");

                if (score == null || documentId == null) {
                    continue;
                }

                // 精确过滤：必须在分数范围内
                if (score < minScore - SCORE_SAME_THRESHOLD || score > maxScore + SCORE_SAME_THRESHOLD) {
                    continue;
                }

                // 相同分数时，按ID过滤
                if (Math.abs(score - minScore) <= SCORE_SAME_THRESHOLD && lastId != null && documentId > lastId) {
                    continue;
                }

                // 跳过已返回的文档
                if (returnedIds != null && returnedIds.contains(documentId)) {
                    continue;
                }

                filteredIds.add(documentId);
                filteredCount++;

                if (filteredIds.size() >= limit * 2) {
                    break;
                }
            }

            if (filteredIds.isEmpty()) {
                log.debug("向量搜索过滤后没有符合条件的文档");
                return documents;
            }

            // 批量查询文档
            int batchSize = 50;
            for (int i = 0; i < filteredIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, filteredIds.size());
                List<Long> batchIds = filteredIds.subList(i, endIndex);
                List<KnowledgeDocument> batchDocs = baseMapper.selectBatchIds(batchIds);

                for (KnowledgeDocument document : batchDocs) {
                    if (document == null || document.getStatus() != KnowledgeDocumentStatusEnum.ENABLED.getId()) {
                        continue;
                    }

                    if (StrUtil.isNotBlank(dto.getCategory()) && !dto.getCategory().equals(document.getCategory())) {
                        continue;
                    }

                    // 查找对应的分数
                    for (Document doc : vectorDocs) {
                        Long docId = ParamUtils.parseDocumentId(doc.getMetadata().get("documentId"));
                        if (docId != null && docId.equals(document.getId())) {
                            document.setSimilarityScore((Double) doc.getMetadata().get("similarity"));
                            break;
                        }
                    }

                    documents.add(document);

                    if (documents.size() >= limit) {
                        break;
                    }
                }

                if (documents.size() >= limit) {
                    break;
                }
            }

            // 按分数和ID排序
            documents.sort((a, b) -> {
                Double s1 = a.getSimilarityScore();
                Double s2 = b.getSimilarityScore();
                if (s1 == null && s2 == null) return a.getId().compareTo(b.getId());
                if (s1 == null) return 1;
                if (s2 == null) return -1;
                int cmp = s2.compareTo(s1);
                return cmp != 0 ? cmp : a.getId().compareTo(b.getId());
            });

            // 限制返回数量
            if (documents.size() > limit) {
                documents = documents.subList(0, limit);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.debug("fetchDocumentsByVectorSearch完成，耗时: {}ms，向量查询: {}条，过滤: {}条，返回: {}条，分数范围: [{}, {}]",
                    duration, vectorQueryCount, filteredCount, documents.size(), minScore, maxScore);

        } catch (Exception e) {
            log.error("通过向量搜索获取文档失败", e);
        }

        return documents;
    }

    /**
     * 添加分页状态信息到结果中
     */
    private void addPaginationStatus(Map<String, Object> result, CursorUtils.PaginationPath path) {
        if (path == null) {
            return;
        }

        // 添加分页状态信息
        Map<String, Object> paginationStatus = new HashMap<>();
        paginationStatus.put("current_page", path.getCurrentPageIndex());  // 当前页码
        paginationStatus.put("total_pages", path.getTotalPageCount());  // 总页数
        paginationStatus.put("earliest_available_page", path.getFirstPageIndex());  // 可回溯的最早页码（始终为1）

        result.put("pagination_status", paginationStatus);
    }

    /**
     * 关键词搜索模式（传统分页）
     */
    private List<KnowledgeDocument> searchWithKeywordMode(KnowledgeDocumentSearchDto dto, Map<String, Object> result) {
        List<KnowledgeDocument> documents = keywordSearch(dto);
        if (documents == null) documents = new ArrayList<>();

        // 传统分页信息
        int pageSize = dto.getSize();
        int currentPage = dto.getPage();
        int offset = (currentPage - 1) * pageSize;
        int total = documents.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 应用分页
        if (offset < documents.size()) {
            documents = new ArrayList<>(documents.subList(offset, Math.min(offset + pageSize, documents.size())));
        }

        result.put("total_count", total);
        result.put("total_pages", totalPages);
        result.put("has_next", currentPage < totalPages);
        result.put("has_previous", currentPage > 1);

        return documents;
    }

    /**
     * 添加分页状态信息到结果中
     */
    private void addPaginationStatus(Map<String, Object> result, CursorUtils.PaginationPath path) {
        if (path == null) {
            return;
        }

        // 添加分页状态信息
        Map<String, Object> paginationStatus = new HashMap<>();
        paginationStatus.put("current_page", path.getCurrentPageIndex());  // 当前页码
        paginationStatus.put("total_pages", path.getTotalPageCount());  // 总页数
        paginationStatus.put("earliest_available_page", path.getFirstPageIndex());  // 可回溯的最早页码（始终为1）

        result.put("pagination_status", paginationStatus);
    }

    /**
     * 混合搜索模式（向量+关键词，传统分页）
     */
    private List<KnowledgeDocument> searchWithHybridMode(KnowledgeDocumentSearchDto dto, Map<String, Object> result) {
        int pageSize = dto.getSize();
        int currentPage = dto.getPage();
        int offset = (currentPage - 1) * pageSize;

        // 步骤1：向量搜索（获取高分文档）
        CursorSearchResultVO vectorResult = vectorSearch(dto);
        List<KnowledgeDocument> documents = vectorResult.getDocuments();

        // 步骤2：只在第一页且向量结果不足时补充关键词搜索
        if (documents.size() < pageSize && currentPage == 1) {
            List<KnowledgeDocument> kwDocs = keywordSearch(dto);
            if (kwDocs != null && !kwDocs.isEmpty()) {
                // 性能优化：只检查前N条ID
                Set<Long> existingIds = documents.stream()
                        .limit(pageSize * 2)
                        .map(KnowledgeDocument::getId)
                        .collect(Collectors.toSet());

                // 补充不重复的文档
                kwDocs.stream()
                        .filter(d -> !existingIds.contains(d.getId()))
                        .limit(pageSize * 2)
                        .forEach(documents::add);
            }
        }

        // 步骤3：只在有分数的文档中，相同分数按ID排序，保留向量搜索和关键词搜索的原始顺序
        sortDocumentsWithScoreOnly(documents);

        // 步骤4：提前分页，只对当前页+下一页的结果排序
        int endIndex = Math.min(offset + pageSize * 2, documents.size());
        if (offset < documents.size()) {
            // 只对需要处理的数据进行分页
            documents = new ArrayList<>(documents.subList(offset, Math.min(offset + pageSize, endIndex)));
        } else {
            documents = new ArrayList<>();
        }

        // 步骤5：延迟计算总数（只在需要时）
        int total = documents.size() >= pageSize ? getTotalSearchCount(dto) : documents.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        result.put("total_count", total);
        result.put("total_pages", totalPages);
        result.put("has_next", currentPage < totalPages);
        result.put("has_previous", currentPage > 1);

        return documents;
    }

    /**
     * 添加分页状态信息到结果中
     */
    private void addPaginationStatus(Map<String, Object> result, CursorUtils.PaginationPath path) {
        if (path == null) {
            return;
        }

        // 添加分页状态信息
        Map<String, Object> paginationStatus = new HashMap<>();
        paginationStatus.put("current_page", path.getCurrentPageIndex());  // 当前页码
        paginationStatus.put("total_pages", path.getTotalPageCount());  // 总页数
        paginationStatus.put("earliest_available_page", path.getFirstPageIndex());  // 可回溯的最早页码（始终为1）

        result.put("pagination_status", paginationStatus);
    }

    /**
     * 按分数和ID排序（相同分数时按ID升序）
     */
    private void sortDocumentsByScoreAndId(List<KnowledgeDocument> documents) {
        documents.sort((a, b) -> {
            Double s1 = a.getSimilarityScore(), s2 = b.getSimilarityScore();
            if (s1 == null && s2 == null) return a.getId().compareTo(b.getId());
            if (s1 == null) return 1;
            if (s2 == null) return -1;
            int cmp = s2.compareTo(s1);  // 分数降序
            return cmp != 0 ? cmp : a.getId().compareTo(b.getId());  // 相同则ID升序
        });
    }

    /**
     * 只对有分数的文档进行相同分数的ID排序，保留向量搜索和关键词搜索的原始顺序
     * 混合搜索模式下，向量搜索的文档已按分数降序，关键词搜索的文档按其他排序规则
     * 只处理向量搜索结果中分数相同的文档，不打乱整体排序
     */
    private void sortDocumentsWithScoreOnly(List<KnowledgeDocument> documents) {
        // 找出第一个有分数的文档
        int firstScoreIndex = -1;
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getSimilarityScore() != null) {
                firstScoreIndex = i;
                break;
            }
        }

        // 找出最后一个有分数的文档
        int lastScoreIndex = -1;
        for (int i = documents.size() - 1; i >= 0; i--) {
            if (documents.get(i).getSimilarityScore() != null) {
                lastScoreIndex = i;
                break;
            }
        }

        // 如果没有有分数的文档，不需要排序
        if (firstScoreIndex == -1 || lastScoreIndex == -1) {
            return;
        }

        // 只对有分数的文档进行局部排序
        List<KnowledgeDocument> scoredDocs = documents.subList(firstScoreIndex, lastScoreIndex + 1);
        scoredDocs.sort((a, b) -> {
            Double s1 = a.getSimilarityScore(), s2 = b.getSimilarityScore();
            int cmp = s2.compareTo(s1);  // 分数降序
            return cmp != 0 ? cmp : a.getId().compareTo(b.getId());  // 相同则ID升序
        });
    }

    /**
     * 构建通用响应（文档转换 + 分页信息）
     */
    private void buildCommonResponse(Map<String, Object> result, List<KnowledgeDocument> documents, KnowledgeDocumentSearchDto dto) {
        // 性能优化：大数据量时使用并行流
        List<Map<String, Object>> docResponses = documents.size() > 50 ?
                documents.parallelStream()
                        .map(this::convertToDocumentResponse)
                        .collect(Collectors.toList()) :
                documents.stream()
                        .map(this::convertToDocumentResponse)
                        .collect(Collectors.toList());

        result.put("documents", docResponses);
        result.put("current_page", dto.getPage());
        result.put("page_size", dto.getSize());
    }

    /**
     * 向量搜索（支持游标分页 + 后端去重 + 混合模式处理大结果集）
     * 核心逻辑：
     * 1. 向量模式：基于分数过滤 + ID去重
     * 2. 混合模式：基于分数 + ID双重过滤，解决大结果集相同分数的分页问题
     * 混合模式控制：
     * - 通过 dto.enableHybridMode 标记控制是否启用分数连续性分析
     * - 如果文档ID有序（如按创建时间递增），建议设为false以提高性能
     * - 如果启用混合模式（enableHybridMode=true），会自动检测以下情况并切换：
     *   1. 向量库返回大量结果（>=200条）且所有分数相同
     *   2. 向量库返回大量结果（>=200条）且尾部有大量相同分数块（>=100条）
     */
    private CursorSearchResultVO vectorSearch(KnowledgeDocumentSearchDto dto) {
        List<KnowledgeDocument> results = new ArrayList<>();
        Double minScore = null;
        Double maxScore = null;
        Long lastId = null;
        boolean useHybridMode = false;

        if (StrUtil.isBlank(dto.getKeyword())) {
            return new CursorSearchResultVO(results, minScore, maxScore, lastId, useHybridMode);
        }

        // ========== 第一步：解码游标 ==========
        Double cursorMinScore = null;
        Double cursorMaxScore = null;
        Long lastCursorId = null;
        Set<Long> returnedIds = new HashSet<>();
        boolean hybridModeFromCursor = false;

        if (StrUtil.isNotBlank(dto.getCursor())) {
            try {
                String[] cursorParts = CursorUtils.decodeCursor(dto.getCursor());
                if (cursorParts != null && cursorParts.length >= 2) {
                    // 获取最低分数、最高分数
                    cursorMinScore = Double.parseDouble(cursorParts[0]);
                    cursorMaxScore = Double.parseDouble(cursorParts[1]);
                    
                    // 检查是否是混合模式游标（包含lastId）
                    if (cursorParts.length >= 3 && StrUtil.isNotBlank(cursorParts[2])) {
                        hybridModeFromCursor = true;
                        lastCursorId = Long.parseLong(cursorParts[2]);
                    }
                    
                    // 获取已返回的ID列表
                    if (cursorParts.length >= 4 && StrUtil.isNotBlank(cursorParts[3])) {
                        returnedIds = Arrays.stream(cursorParts[3].split("\\" + ID_SEPARATOR))
                                .filter(StrUtil::isNotBlank)
                                .map(Long::valueOf)
                                .collect(Collectors.toSet());
                    }
                }
            } catch (Exception e) {
                log.warn("解码游标失败，重新开始搜索，cursor: {}", dto.getCursor());
            }
        }

        // 获取游标方向
        String cursorDirection = dto.getCursorDirection() != null ? dto.getCursorDirection() : CURSOR_DIRECTION_FORWARD;

        try {
            // ========== 第二步：执行向量搜索 ==========
            // 根据已返回的文档数量动态调整topK
            int topK = VECTOR_SEARCH_BATCH_SIZE;
            if (returnedIds.size() > 20) {
                topK = Math.min(topK + returnedIds.size(), 200);
            }

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(dto.getKeyword())
                    .topK(topK)
                    .similarityThreshold(0.6)
                    .build();

            List<Document> vectorDocs = customerKnowledgeVectorStore.similaritySearch(searchRequest);
            if (CollUtil.isEmpty(vectorDocs)) {
                return new CursorSearchResultVO(results, minScore, maxScore, lastId, useHybridMode);
            }

            // ========== 第三步：分析分数分布，判断是否使用混合模式 ==========
            // 只有在明确启用混合模式时才进行分数连续性分析
            if (Boolean.TRUE.equals(dto.getEnableHybridMode())) {
                List<Double> scores = new ArrayList<>();
                Map<Double, Integer> scoreFrequency = new HashMap<>(); // key: 分数, value: 该分数出现次数
                
                for (Document doc : vectorDocs) {
                    Double score = (Double) doc.getMetadata().get("similarity");
                    if (score != null) {
                        if (minScore == null || score < minScore) {
                            minScore = score;
                        }
                        if (maxScore == null || score > maxScore) {
                            maxScore = score;
                        }
                        scores.add(score);
                        scoreFrequency.put(score, scoreFrequency.getOrDefault(score, 0) + 1);
                    }
                }

                // 检测连续相同分数的块（从尾部开始）
                int tailSameScoreBlockSize = 0;
                Double tailSameScoreValue = null;

                if (CollUtil.isNotEmpty(scores)) {
                    tailSameScoreValue = scores.get(scores.size() - 1);
                    tailSameScoreBlockSize = 1;
                    
                    // 从后往前遍历，找到尾部连续相同分数的块
                    for (int i = scores.size() - 2; i >= 0; i--) {
                        if (Math.abs(scores.get(i) - tailSameScoreValue) <= SCORE_SAME_THRESHOLD) {
                            tailSameScoreBlockSize++;
                        } else {
                            break;
                        }
                    }
                }

                // 判断是否需要使用混合模式
                boolean allScoresSame = tailSameScoreBlockSize >= scores.size();
                if (vectorDocs.size() >= LARGE_RESULT_THRESHOLD) {
                    if (allScoresSame) { // 情况1：所有分数都相同
                        useHybridMode = true;
                        log.info("【混合模式-全部相同】检测到大结果集且所有分数相同，切换到混合模式。返回文档数: {}, minScore: {}, maxScore: {}", vectorDocs.size(), minScore, maxScore);
                    } else if (tailSameScoreBlockSize >= VECTOR_SEARCH_BATCH_SIZE) { // 情况2：尾部有大量相同分数的块
                        useHybridMode = true;
                        log.info("【混合模式-部分相同】检测到大结果集且尾部有大量相同分数块，切换到混合模式。返回文档数: {}, 相同分数块大小: {}, 分数值: {}, 总分数种类: {}", vectorDocs.size(), tailSameScoreBlockSize, tailSameScoreValue, scoreFrequency.size());
                    }
                } else if (hybridModeFromCursor) { // 情况3：从游标中继承混合模式
                    useHybridMode = true;
                    log.debug("从游标中继承混合模式");
                }
            } else {
                // 不启用混合模式时，只计算minScore和maxScore
                for (Document doc : vectorDocs) {
                    Double score = (Double) doc.getMetadata().get("similarity");
                    if (score != null) {
                        if (minScore == null || score < minScore) {
                            minScore = score;
                        }
                        if (maxScore == null || score > maxScore) {
                            maxScore = score;
                        }
                    }
                }
                // 从游标继承混合模式状态
                useHybridMode = hybridModeFromCursor;
            }

            // ========== 第四步：根据方向和模式过滤文档 ==========
            List<Document> filteredDocs = filterDocsByDirection(
                    vectorDocs, cursorDirection, cursorMinScore, cursorMaxScore,
                    lastCursorId, returnedIds, useHybridMode);

            // ========== 第五步：构建结果 ==========
            for (Document doc : filteredDocs) {
                Map<String, Object> metadata = doc.getMetadata();

                Long documentId = ParamUtils.parseDocumentId(metadata.get("documentId"));

                // 后端去重：跳过已返回的文档ID
                if (returnedIds.contains(documentId)) {
                    continue;
                }

                KnowledgeDocument document = baseMapper.selectById(documentId);
                if (document == null || document.getStatus() != KnowledgeDocumentStatusEnum.ENABLED.getId()) {
                    continue;
                }

                if (StrUtil.isNotBlank(dto.getCategory()) && !dto.getCategory().equals(document.getCategory())) {
                    continue;
                }

                document.setSimilarityScore((Double) metadata.get("similarity"));
                results.add(document);

                // 记录最后一个文档的ID（用于混合模式）
                lastId = documentId;

                if (results.size() >= dto.getSize()) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("向量搜索失败", e);
        }

        return new CursorSearchResultVO(results, minScore, maxScore, lastId, useHybridMode);
    }

    /**
     * 根据方向和模式过滤文档
     * 
     * @param docs 向量库返回的文档列表
     * @param direction 游标方向（forward/backward/first）
     * @param minThreshold 最小分数阈值（forward方向使用）
     * @param maxThreshold 最大分数阈值（backward方向使用）
     * @param lastCursorId 上次返回的最后一个文档ID（混合模式使用）
     * @param returnedIds 已返回的文档ID集合
     * @param useHybridMode 是否使用混合模式
     * @return 过滤后的文档列表
     */
    private List<Document> filterDocsByDirection(List<Document> docs, String direction,
                                                 Double minThreshold, Double maxThreshold,
                                                 Long lastCursorId, Set<Long> returnedIds, boolean useHybridMode) {
        List<Document> filtered = new ArrayList<>();

        for (Document doc : docs) {
            Map<String, Object> metadata = doc.getMetadata();

            // 获取分数
            Double score = (Double) metadata.get("similarity");
            if (score == null) {
                continue;
            }

            // 获取文档ID
            Object documentIdObj = metadata.get("documentId");
            if (documentIdObj == null) {
                continue;
            }

            Long documentId = ParamUtils.parseDocumentId(documentIdObj);
            if (documentId == null) {
                continue;
            }

            if (useHybridMode && lastCursorId != null) { // 混合模式：使用分数 + ID双重过滤
                if (shouldIncludeInHybridMode(direction, score, documentId, minThreshold, maxThreshold, lastCursorId)) {
                    filtered.add(doc);
                }
            } else { // 普通模式：不使用分数过滤，只依赖后续的ID去重
                filtered.add(doc);
            }
        }

        // 排序逻辑
        if (useHybridMode) {  // 混合模式：按ID排序
            sortDocumentsById(filtered, direction);
        } else if (CURSOR_DIRECTION_BACKWARD.equals(direction)) {  // backward方向：按分数降序
            sortDocumentsByScore(filtered, true);
        }
        // forward方向：保持向量库原始顺序（已按分数降序）

        return filtered;
    }

    /**
     * 混合模式下判断文档是否应该包含
     * 
     * 游标中存储的分数说明：
     * - minThreshold/minScore: 向量库当前批次的最小分数（最低相似度）
     * - maxThreshold/maxScore: 向量库当前批次的最大分数（最高相似度）
     * 
     * Vector Store 返回分数降序排列，从高到低
     * 
     * Forward（下一页）: 跳过当前页已返回的，找分数更小的文档
     *   - score < minThreshold: 分数小于当前批次最小值的文档
     *   - OR (score ≈ minThreshold AND id > lastCursorId): 相同分数但ID更大
     * 
     * Backward（上一页）: 找分数更大的文档（回溯到高分部分）
     *   - score > maxThreshold: 分数大于当前批次最大值的文档
     *   - OR (score ≈ maxThreshold AND id < lastCursorId): 相同分数但ID更小
     */
    private boolean shouldIncludeInHybridMode(String direction, Double score, Long documentId,
                                              Double minThreshold, Double maxThreshold, Long lastCursorId) {
        if (CURSOR_DIRECTION_FORWARD.equals(direction)) {
            // Forward: 向前翻页，找分数更小的文档（因为向量库是降序排列）
            if (minThreshold == null) {
                return true;
            }
            // 条件：分数更小 或 (分数相同且ID更大)
            return score < minThreshold || 
                   (Math.abs(score - minThreshold) <= SCORE_SAME_THRESHOLD && documentId > lastCursorId);
        } else if (CURSOR_DIRECTION_BACKWARD.equals(direction)) {
            // Backward: 向后翻页，找分数更大的文档
            if (maxThreshold == null) {
                return true;
            }
            // 条件：分数更大 或 (分数相同且ID更小)
            return score > maxThreshold || 
                   (Math.abs(score - maxThreshold) <= SCORE_SAME_THRESHOLD && documentId < lastCursorId);
        }
        return true; // first方向包含所有
    }

    /**
     * 按分数排序
     * @param ascending 是否升序
     */
    private void sortDocumentsByScore(List<Document> docs, boolean ascending) {
        docs.sort((d1, d2) -> {
            Double s1 = (Double) d1.getMetadata().get("similarity");
            Double s2 = (Double) d2.getMetadata().get("similarity");
            return ascending ? s1.compareTo(s2) : s2.compareTo(s1);
        });
    }

    /**
     * 按ID排序
     * @param forward 是否forward方向（升序），backward则为降序
     */
    private void sortDocumentsById(List<Document> docs, String direction) {
        boolean ascending = CURSOR_DIRECTION_FORWARD.equals(direction);
        docs.sort((d1, d2) -> {
            Long id1 = ParamUtils.parseDocumentId(d1.getMetadata().get("documentId"));
            Long id2 = ParamUtils.parseDocumentId(d2.getMetadata().get("documentId"));
            return ascending ? id1.compareTo(id2) : id2.compareTo(id1);
        });
    }

    /**
     * 关键词搜索
     */
    private List<KnowledgeDocument> keywordSearch(KnowledgeDocumentSearchDto dto) {
        // 构建查询条件
        Map<String, Object> condition = new HashMap<>();

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            condition.put("keyword", dto.getKeyword());
        }

        if (StrUtil.isNotBlank(dto.getCategory())) {
            condition.put("category", dto.getCategory());
        }

        if (dto.getStatus() != null) {
            condition.put("status", dto.getStatus());
        } else {
            condition.put("status", 1); // 默认只查启用的
        }

        if (StrUtil.isNotBlank(dto.getAuthor())) {
            condition.put("author", dto.getAuthor());
        }

        // 分页参数
        int offset = (dto.getPage() - 1) * dto.getSize();
        condition.put("offset", offset);
        condition.put("limit", dto.getSize());

        // 排序
        if (Boolean.TRUE.equals(dto.getOrderByPriority())) {
            condition.put("order_by", "priority");
            condition.put("order_desc", true);
        } else if (Boolean.TRUE.equals(dto.getOrderByViewCount())) {
            condition.put("order_by", "view_count");
            condition.put("order_desc", true);
        } else {
            condition.put("order_by", "create_time");
            condition.put("order_desc", true);
        }

        try {
            List<KnowledgeDocument> result = baseMapper.selectByCondition(condition);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("关键词搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取搜索总数
     */
    private int getTotalSearchCount(KnowledgeDocumentSearchDto dto) {
        Map<String, Object> condition = new HashMap<>();

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            condition.put("keyword", dto.getKeyword());
        }

        if (StrUtil.isNotBlank(dto.getCategory())) {
            condition.put("category", dto.getCategory());
        }

        if (dto.getStatus() != null) {
            condition.put("status", dto.getStatus());
        } else {
            condition.put("status", 1);
        }

        if (StrUtil.isNotBlank(dto.getAuthor())) {
            condition.put("author", dto.getAuthor());
        }

        try {
            return baseMapper.countByCondition(condition);
        } catch (Exception e) {
            log.error("获取搜索总数失败", e);
            return 0;
        }
    }

    /**
     * 带游标分页的相似度搜索（支持双向分页）
     * @param query 查询文本
     * @param limit 每页条数
     * @param cursor 游标字符串（第一次查询为null）
     * @param cursorDirection 游标方向：forward(下一页), backward(上一页), first(首页)
     * @param similarityThreshold 相似度阈值
     * @return 包含结果和游标信息的Map
     */
    @Override
    public Map<String, Object> similaritySearch(String query, int limit, String cursor, String cursorDirection, double similarityThreshold) {
        return similaritySearch(query, limit, cursor, cursorDirection, similarityThreshold, false);
    }

    /**
     * 带游标分页的相似度搜索（支持双向分页 + 混合模式控制 + 无限分页）
     * 
     * 优化：
     * - 支持向前、向后、首页滚动分页，保证分页数据准确
     * - 使用Redis存储分页路径，支持无限滚动
     * - 支持path容错，即使pathId过期或传错也能正常显示
     * 
     * @param query 查询文本
     * @param limit 每页条数
     * @param cursor 游标字符串（第一次查询为null）
     * @param cursorDirection 游标方向：forward(下一页), backward(上一页), first(首页)
     * @param similarityThreshold 相似度阈值
     * @param enableHybridMode 是否启用混合模式（分数连续性分析），如果文档ID有序建议设为false
     * @return 包含结果和游标信息的Map
     */
    @Override
    public Map<String, Object> similaritySearch(String query, int limit, String cursor, String cursorDirection, double similarityThreshold, Boolean enableHybridMode) {
        Map<String, Object> result = new HashMap<>();
        if (StrUtil.isBlank(query)) {
            result.put("documents", new ArrayList<>());
            result.put("next_cursor", null);
            result.put("previous_cursor", null);
            result.put("has_next", false);
            result.put("has_previous", false);
            return result;
        }

        if (StrUtil.isBlank(cursorDirection)) {
            cursorDirection = CURSOR_DIRECTION_FORWARD;
        }

        long startTime = System.currentTimeMillis();

        try {
            // ========== 第一步：处理分页路径 ==========
            String pathId = null;
            boolean pathExpired = false;
            CursorUtils.PaginationPath path = new CursorUtils.PaginationPath();

            // 尝试从cursor中解析pathId
            if (StrUtil.isNotBlank(cursor)) {
                try {
                    String[] cursorParts = CursorUtils.decodeCursor(cursor);
                    if (cursorParts != null && cursorParts.length >= 5) {
                        // 新格式：cursorParts[4] 是 pathId
                        pathId = cursorParts[4];
                    }
                } catch (Exception e) {
                    log.warn("从cursor中解析pathId失败，cursor: {}", cursor);
                }
            }

            // 从Redis加载分页路径
            if (StrUtil.isNotBlank(pathId)) {
                path = paginationPathManager.loadPath(pathId);
                if (path == null || path.getCurrentPage() == null) {
                    log.info("分页路径不存在或已过期，pathId: {}，重新开始查询", pathId);
                    pathExpired = true;
                    pathId = null;
                    path = new CursorUtils.PaginationPath();
                    cursorDirection = CURSOR_DIRECTION_FIRST;
                }
            } else {
                pathId = paginationPathManager.generatePathId();
            }

            // ========== 第二步：解析游标中的分页信息 ==========
            Set<Long> returnedIds = new HashSet<>();
            Double cursorMinScore = null;
            Double cursorMaxScore = null;
            Long lastCursorId = null;
            
            if (StrUtil.isNotBlank(cursor)) {
                try {
                    String[] cursorParts = CursorUtils.decodeCursor(cursor);
                    if (cursorParts != null && cursorParts.length >= 2) {
                        cursorMinScore = Double.parseDouble(cursorParts[0]);
                        cursorMaxScore = Double.parseDouble(cursorParts[1]);
                        
                        if (cursorParts.length >= 3 && StrUtil.isNotBlank(cursorParts[2])) {
                            lastCursorId = Long.parseLong(cursorParts[2]);
                        }
                        
                        if (cursorParts.length >= 4 && StrUtil.isNotBlank(cursorParts[3])) {
                            returnedIds = Arrays.stream(cursorParts[3].split("\\" + ID_SEPARATOR))
                                    .filter(StrUtil::isNotBlank)
                                    .map(Long::valueOf)
                                    .collect(Collectors.toSet());
                        }
                    }
                } catch (Exception e) {
                    log.warn("解码游标失败，重新开始搜索，cursor: {}", cursor);
                    returnedIds = new HashSet<>();
                    cursorMinScore = null;
                    cursorMaxScore = null;
                    lastCursorId = null;
                }
            }

            // ========== 第三步：根据分页方向处理 ==========
            List<Map<String, Object>> documents;
            CursorUtils.PageInfo currentPageInfo = path.getCurrentPage();

            if (pathExpired) {
                // path过期：重新查询第一页
                documents = performVectorSearch(query, limit, similarityThreshold, enableHybridMode, result, returnedIds, pathId);
                
                if (documents != null && !documents.isEmpty()) {
                    // 保存到分页路径
                    saveToPaginationPath(path, documents, result);
                    paginationPathManager.savePath(pathId, path);
                    
                    result.put("path_id", pathId);
                }
                
                log.info("路径已过期，重新查询第一页，耗时: {}ms，返回文档数: {}", System.currentTimeMillis() - startTime, documents != null ? documents.size() : 0);
            } else if (CURSOR_DIRECTION_BACKWARD.equals(cursorDirection) && path.hasPrevious()) {
                // Backward：返回上一页（从路径中获取）
                CursorUtils.PageInfo prevPage = path.getPreviousPage();
                path.currentIndex--;  // 移动到上一页

                documents = fetchDocumentsByPageInfoForSimilarity(prevPage, query, limit, result);

                // 判断has_next：是否有下一页（即能否再向前翻回）
                boolean hasNext = path.hasNext();

                result.put("next_cursor", hasNext ? encodePaginationPathWithPage(path) : null);
                result.put("previous_cursor", path.hasPrevious() ? encodePaginationPathWithPage(path) : null);
                result.put("has_next", hasNext);
                result.put("has_previous", path.hasPrevious());

                // 保存分页路径到Redis
                paginationPathManager.savePath(pathId, path);

                log.debug("Backward分页完成，耗时: {}ms，返回文档数: {}，has_next: {}", System.currentTimeMillis() - startTime, documents.size(), hasNext);
            } else if (CURSOR_DIRECTION_FIRST.equals(cursorDirection)) {
                // First：从向量库获取第一页
                documents = performVectorSearch(query, limit, similarityThreshold, enableHybridMode, result, returnedIds, pathId);
                
                if (documents != null && !documents.isEmpty()) {
                    // 清空path并保存新数据
                    path = new CursorUtils.PaginationPath();
                    saveToPaginationPath(path, documents, result);
                    paginationPathManager.savePath(pathId, path);
                    
                    result.put("path_id", pathId);
                }
                
                log.debug("First查询完成，耗时: {}ms，返回文档数: {}", System.currentTimeMillis() - startTime, documents.size());
            } else {
                // Forward：获取下一页
                documents = performVectorSearch(query, limit, similarityThreshold, enableHybridMode, result, returnedIds, pathId);
                
                if (documents != null && !documents.isEmpty()) {
                    saveToPaginationPath(path, documents, result);
                    paginationPathManager.savePath(pathId, path);
                    
                    result.put("path_id", pathId);
                }
                
                log.debug("Forward分页完成，耗时: {}ms，返回文档数: {}", System.currentTimeMillis() - startTime, documents.size());
            }

        } catch (Exception e) {
            log.error("相似度搜索失败，query: {}, limit: {}, cursor: {}, direction: {}", query, limit, cursor, cursorDirection, e);
            result.put("error", "搜索失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行向量搜索
     */
    private List<Map<String, Object>> performVectorSearch(String query, int limit, double similarityThreshold, 
                                                           Boolean enableHybridMode, Map<String, Object> result,
                                                           Set<Long> returnedIds, String pathId) {
        // 向量搜索
        int topK = Math.min(limit + returnedIds.size() * 2, 200);
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        List<Document> vectorDocs = customerKnowledgeVectorStore.similaritySearch(request);
        if (CollUtil.isEmpty(vectorDocs)) {
            return new ArrayList<>();
        }

        // 分析分数分布，判断是否使用混合模式
        boolean useHybridMode = analyzeScoreDistribution(vectorDocs, enableHybridMode);

        // 根据方向过滤文档
        List<Document> filteredDocs = filterDocsForForward(vectorDocs, returnedIds, useHybridMode);

        // 构建结果
        List<Map<String, Object>> documents = new ArrayList<>();
        Double currentMinScore = null;
        Double currentMaxScore = null;
        Long lastId = null;

        for (Document doc : filteredDocs) {
            Map<String, Object> metadata = doc.getMetadata();
            Object documentIdObj = metadata.get("documentId");
            if (documentIdObj == null) {
                continue;
            }

            Long documentId = ParamUtils.parseDocumentId(documentIdObj);
            if (documentId == null) {
                continue;
            }

            // 去重
            if (returnedIds.contains(documentId)) {
                continue;
            }

            Double score = (Double) metadata.get("similarity");
            if (currentMinScore == null || score < currentMinScore) {
                currentMinScore = score;
            }
            if (currentMaxScore == null || score > currentMaxScore) {
                currentMaxScore = score;
            }

            Map<String, Object> docResult = new HashMap<>();
            docResult.put("id", documentId);
            docResult.put("title", metadata.get("title"));
            docResult.put("content", doc.getText());
            docResult.put("summary", metadata.get("summary"));
            docResult.put("category", metadata.get("category"));
            docResult.put("tags", metadata.get("tags"));
            docResult.put("similarity_score", score);
            docResult.put("create_time", metadata.get("create_time"));

            documents.add(docResult);
            lastId = documentId;

            if (documents.size() >= limit) {
                break;
            }
        }

        // 相同分数按ID排序
        sortResultsByScoreAndId(documents);

        // 保存分页信息到result
        savePaginationInfoToResult(result, documents, currentMinScore, currentMaxScore, lastId, returnedIds, useHybridMode);

        return documents;
    }

    /**
     * 分析分数分布，判断是否使用混合模式
     */
    private boolean analyzeScoreDistribution(List<Document> vectorDocs, Boolean enableHybridMode) {
        if (!Boolean.TRUE.equals(enableHybridMode)) {
            return false;
        }

        List<Double> scores = new ArrayList<>();
        for (Document doc : vectorDocs) {
            Double score = (Double) doc.getMetadata().get("similarity");
            if (score != null) {
                scores.add(score);
            }
        }

        if (scores.isEmpty()) {
            return false;
        }

        // 检测连续相同分数的块
        int tailSameScoreBlockSize = 0;
        Double tailSameScoreValue = scores.get(scores.size() - 1);
        tailSameScoreBlockSize = 1;

        for (int i = scores.size() - 2; i >= 0; i--) {
            if (Math.abs(scores.get(i) - tailSameScoreValue) <= SCORE_SAME_THRESHOLD) {
                tailSameScoreBlockSize++;
            } else {
                break;
            }
        }

        // 判断是否需要使用混合模式
        boolean allScoresSame = tailSameScoreBlockSize >= scores.size();
        if (vectorDocs.size() >= LARGE_RESULT_THRESHOLD) {
            if (allScoresSame || tailSameScoreBlockSize >= VECTOR_SEARCH_BATCH_SIZE) {
                return true;
            }
        }

        return false;
    }

    /**
     * 过滤文档（forward方向）
     */
    private List<Document> filterDocsForForward(List<Document> vectorDocs, Set<Long> returnedIds, boolean useHybridMode) {
        List<Document> filtered = new ArrayList<>();
        for (Document doc : vectorDocs) {
            Object documentIdObj = doc.getMetadata().get("documentId");
            if (documentIdObj == null) {
                continue;
            }

            Long documentId = ParamUtils.parseDocumentId(documentIdObj);
            if (documentId == null) {
                continue;
            }

            // 去重
            if (returnedIds.contains(documentId)) {
                continue;
            }

            filtered.add(doc);
        }

        return filtered;
    }

    /**
     * 保存分页信息到result
     */
    private void savePaginationInfoToResult(Map<String, Object> result, List<Map<String, Object>> documents,
                                            Double currentMinScore, Double currentMaxScore, Long lastId,
                                            Set<Long> returnedIds, boolean useHybridMode, String pathId) {
        int limit = documents.size();

        // 生成游标（使用编码后的paginationPath）
        if (documents.size() >= limit) {
            // 生成传统的cursor（兼容性）
            Set<Long> allIds = new HashSet<>(returnedIds);
            for (Map<String, Object> doc : documents) {
                allIds.add((Long) doc.get("id"));
            }
            Double nextMinScore = useHybridMode ? currentMinScore : currentMinScore;
            String traditionalCursor = CursorUtils.encodeCursor(nextMinScore, currentMaxScore, lastId, allIds, pathId);
            result.put("next_cursor", traditionalCursor);
            result.put("has_next", true);
        } else {
            result.put("next_cursor", null);
            result.put("has_next", false);
        }

        result.put("previous_cursor", null);
        result.put("has_previous", false);
    }

    /**
     * 保存文档到分页路径
     */
    private void saveToPaginationPath(CursorUtils.PaginationPath path, List<Map<String, Object>> documents, Map<String, Object> result) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        Set<Long> currentIds = documents.stream()
                .map(doc -> (Long) doc.get("id"))
                .collect(Collectors.toSet());

        Double minScore = null;
        Double maxScore = null;
        Long lastId = null;

        for (Map<String, Object> doc : documents) {
            Double score = (Double) doc.get("similarity_score");
            if (minScore == null || (score != null && score < minScore)) {
                minScore = score;
            }
            if (maxScore == null || (score != null && score > maxScore)) {
                maxScore = score;
            }
            lastId = (Long) doc.get("id");
        }

        CursorUtils.PageInfo pageInfo = new CursorUtils.PageInfo(minScore, maxScore, lastId, currentIds);
        path.addPageFromPageInfo(pageInfo);

        // 更新result中的游标
        result.put("next_cursor", CursorUtils.encodePaginationPath(path));
        result.put("previous_cursor", path.hasPrevious() ? CursorUtils.encodePaginationPath(path) : null);
        result.put("has_next", true);
        result.put("has_previous", path.hasPrevious());
    }

    /**
     * 根据PageInfo获取文档（用于similaritySearch的backward分页）
     */
    private List<Map<String, Object>> fetchDocumentsByPageInfoForSimilarity(CursorUtils.PageInfo pageInfo, String query, 
                                                                              int limit, Map<String, Object> result) {
        List<Map<String, Object>> documents = new ArrayList<>();

        try {
            List<Long> sortedIds = pageInfo.sortedIds;
            Set<Long> returnedIds = pageInfo.ids;

            // 优先使用有序的ID列表
            if (sortedIds != null && !sortedIds.isEmpty()) {
                log.debug("使用保存的有序ID列表查询文档，ID数量: {}", sortedIds.size());

                int batchSize = 50;
                for (int i = 0; i < sortedIds.size() && documents.size() < limit; i += batchSize) {
                    int endIndex = Math.min(i + batchSize, sortedIds.size());
                    List<Long> batchIds = sortedIds.subList(i, endIndex);
                    List<KnowledgeDocument> batchDocs = baseMapper.selectBatchIds(batchIds);

                    for (KnowledgeDocument document : batchDocs) {
                        if (document == null || document.getStatus() != KnowledgeDocumentStatusEnum.ENABLED.getId()) {
                            continue;
                        }

                        Map<String, Object> docResult = new HashMap<>();
                        docResult.put("id", document.getId());
                        docResult.put("title", document.getTitle());
                        docResult.put("content", document.getContent());
                        docResult.put("summary", document.getSummary());
                        docResult.put("category", document.getCategory());
                        docResult.put("similarity_score", (pageInfo.minScore + pageInfo.maxScore) / 2);
                        docResult.put("create_time", document.getCreateTime());

                        documents.add(docResult);

                        if (documents.size() >= limit) {
                            break;
                        }
                    }
                }

                log.debug("fetchDocumentsByPageInfoForSimilarity完成（使用有序ID列表），返回文档数: {}", documents.size());
                return documents;
            }

            // 如果有序ID列表为空，重新查询向量库
            log.debug("有序ID列表为空，尝试重新查询向量库");
            return fetchDocumentsByVectorSearchForSimilarity(pageInfo, query, limit);

        } catch (Exception e) {
            log.error("根据PageInfo获取文档失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 通过向量搜索获取文档（用于similaritySearch的backward分页）
     */
    private List<Map<String, Object>> fetchDocumentsByVectorSearchForSimilarity(CursorUtils.PageInfo pageInfo, String query, int limit) {
        List<Map<String, Object>> documents = new ArrayList<>();

        try {
            Double minScore = pageInfo.minScore;
            Double maxScore = pageInfo.maxScore;
            Long lastId = pageInfo.lastId;
            Set<Long> returnedIds = pageInfo.ids;

            if (minScore == null || maxScore == null) {
                log.debug("分数范围不完整，无法查询向量库");
                return documents;
            }

            // 向量搜索
            Double scoreRange = maxScore - minScore;
            int topK = 200;
            if (scoreRange < 0.01) {
                topK = 300;
            } else if (scoreRange > 0.2) {
                topK = 100;
            }

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(Math.max(0.6, minScore - 0.1))
                    .build();

            List<Document> vectorDocs = customerKnowledgeVectorStore.similaritySearch(searchRequest);

            if (CollUtil.isEmpty(vectorDocs)) {
                log.debug("向量搜索结果为空");
                return documents;
            }

            // 过滤文档
            List<Long> filteredIds = new ArrayList<>();
            for (Document doc : vectorDocs) {
                Map<String, Object> metadata = doc.getMetadata();
                Long documentId = ParamUtils.parseDocumentId(metadata.get("documentId"));
                Double score = (Double) metadata.get("similarity");

                if (score == null || documentId == null) {
                    continue;
                }

                // 精确过滤：必须在分数范围内
                if (score < minScore - SCORE_SAME_THRESHOLD || score > maxScore + SCORE_SAME_THRESHOLD) {
                    continue;
                }

                // 相同分数时，按ID过滤
                if (Math.abs(score - minScore) <= SCORE_SAME_THRESHOLD && lastId != null && documentId > lastId) {
                    continue;
                }

                // 跳过已返回的文档
                if (returnedIds != null && returnedIds.contains(documentId)) {
                    continue;
                }

                filteredIds.add(documentId);

                if (filteredIds.size() >= limit * 2) {
                    break;
                }
            }

            if (filteredIds.isEmpty()) {
                log.debug("向量搜索过滤后没有符合条件的文档");
                return documents;
            }

            // 批量查询文档
            int batchSize = 50;
            for (int i = 0; i < filteredIds.size() && documents.size() < limit; i += batchSize) {
                int endIndex = Math.min(i + batchSize, filteredIds.size());
                List<Long> batchIds = filteredIds.subList(i, endIndex);
                List<KnowledgeDocument> batchDocs = baseMapper.selectBatchIds(batchIds);

                for (KnowledgeDocument document : batchDocs) {
                    if (document == null || document.getStatus() != KnowledgeDocumentStatusEnum.ENABLED.getId()) {
                        continue;
                    }

                    // 查找对应的分数
                    Double docScore = null;
                    for (Document doc : vectorDocs) {
                        Long docId = ParamUtils.parseDocumentId(doc.getMetadata().get("documentId"));
                        if (docId != null && docId.equals(document.getId())) {
                            docScore = (Double) doc.getMetadata().get("similarity");
                            break;
                        }
                    }

                    Map<String, Object> docResult = new HashMap<>();
                    docResult.put("id", document.getId());
                    docResult.put("title", document.getTitle());
                    docResult.put("content", document.getContent());
                    docResult.put("summary", document.getSummary());
                    docResult.put("category", document.getCategory());
                    docResult.put("similarity_score", docScore);
                    docResult.put("create_time", document.getCreateTime());

                    documents.add(docResult);

                    if (documents.size() >= limit) {
                        break;
                    }
                }
            }

            // 按分数和ID排序
            sortResultsByScoreAndId(documents);

            // 限制返回数量
            if (documents.size() > limit) {
                documents = documents.subList(0, limit);
            }

            log.debug("fetchDocumentsByVectorSearchForSimilarity完成，返回文档数: {}", documents.size());

        } catch (Exception e) {
            log.error("通过向量搜索获取文档失败", e);
        }

        return documents;
    }

    /**
     * 编码分页路径（包含pathId）
     */
    private String encodePaginationPathWithPage(CursorUtils.PaginationPath path) {
        // 这里需要扩展cursor编码，添加pathId
        // 暂时使用现有的encodePaginationPath
        return CursorUtils.encodePaginationPath(path);
    }

    /**
     * 带传统分页的相似度搜索（page/size）
     * 
     * 使用 offset/limit 进行分页，适用于小数据量场景
     * 注意：深度分页性能较差，建议使用游标分页（similaritySearchWithCursor）
     * 
     * @param query 查询文本
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @param similarityThreshold 相似度阈值
     * @return 包含结果和分页信息的Map
     */
    public Map<String, Object> similaritySearchWithPage(String query, int page, int size, double similarityThreshold) {
        int MAX_PAGE = 10;
        int MAX_TOP_K = 200;

        // 空查询校验
        if (StrUtil.isBlank(query)) {
            return buildPageResult(new ArrayList<>(), 0, page, size, 0);
        }

        // 深度分页限制
        if (page > MAX_PAGE) {
            log.warn("similaritySearchWithPage分页超过推荐最大页数，page: {}, maxPage: {}", page, MAX_PAGE);
            Map<String, Object> result = buildPageResult(new ArrayList<>(), 0, page, size, MAX_PAGE);
            result.put("warning", "深度分页性能差，建议使用游标分页模式");
            return result;
        }

        try {
            // 向量搜索
            int offset = (page - 1) * size;
            int topK = Math.min(offset + size, MAX_TOP_K);
            
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();

            List<Document> vectorResults = customerKnowledgeVectorStore.similaritySearch(request);
            int totalCount = vectorResults.size();

            // 提前计算分页信息
            int totalPages = (int) Math.ceil((double) totalCount / size);

            // 提前返回空结果
            if (offset >= totalCount) {
                return buildPageResult(new ArrayList<>(), totalCount, page, size, totalPages);
            }

            // 分页切片
            int endIndex = Math.min(offset + size, totalCount);
            List<Document> pagedResults = vectorResults.subList(offset, endIndex);

            // 转换并排序
            List<Map<String, Object>> documents = convertDocuments(pagedResults);
            sortResultsByScoreAndId(documents);

            // 构建结果
            Map<String, Object> result = buildPageResult(documents, totalCount, page, size, totalPages);

            // 深度分页提示
            if (page > 5) {
                result.put("performance_tip", "当前已进入深度分页，建议使用游标分页模式以获得更好性能");
            }

            return result;

        } catch (Exception e) {
            log.error("相似度搜索失败，query: {}, page: {}, size: {}", query, page, size, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "搜索失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 构建分页结果
     */
    private Map<String, Object> buildPageResult(List<Map<String, Object>> documents, 
                                                  int totalCount, int page, int size, int totalPages) {
        Map<String, Object> result = new HashMap<>();
        result.put("documents", documents);
        result.put("total_count", totalCount);
        result.put("current_page", page);
        result.put("page_size", size);
        result.put("total_pages", totalPages);
        result.put("has_next", page < totalPages);
        result.put("has_previous", page > 1);
        return result;
    }

    /**
     * 转换文档列表为结果格式
     */
    private List<Map<String, Object>> convertDocuments(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", metadata.get("documentId"));
                    result.put("title", metadata.get("title"));
                    result.put("content", doc.getText());
                    result.put("summary", metadata.get("summary"));
                    result.put("category", metadata.get("category"));
                    result.put("tags", metadata.get("tags"));
                    result.put("similarity_score", metadata.get("similarity"));
                    result.put("create_time", metadata.get("create_time"));
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 对搜索结果按分数和ID排序
     */
    private void sortResultsByScoreAndId(List<Map<String, Object>> results) {
        results.sort((a, b) -> {
            Double s1 = (Double) a.get("similarity_score");
            Double s2 = (Double) b.get("similarity_score");

            if (s1 == null && s2 == null) {
                Long id1 = (Long) a.get("id");
                Long id2 = (Long) b.get("id");
                return id1 != null && id2 != null ? id1.compareTo(id2) : 0;
            }

            if (s1 == null) return 1;
            if (s2 == null) return -1;

            int cmp = s2.compareTo(s1);  // 分数降序
            if (cmp != 0) return cmp;

            // 分数相同：按ID升序
            Long id1 = (Long) a.get("id");
            Long id2 = (Long) b.get("id");
            return id1 != null && id2 != null ? id1.compareTo(id2) : 0;
        });
    }

    /**
     * 缓存结果
     */
    private void cacheResult(String cacheKey, Map<String, Object> result) {
        try {
            redisTemplate.opsForValue().set(cacheKey, result, 300, TimeUnit.SECONDS); // 5分钟
        } catch (Exception e) {
            log.warn("缓存结果失败，key: {}", cacheKey, e);
        }
    }

    /**
     * 关键词从缓存获取
     */
    private Map<String, Object> getKeywordFromCache(String cacheKey) {
        try {
            return (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取失败，key: {}", cacheKey, e);
            return null;
        }
    }

    /**
     * 清理文档缓存
     */
    private void clearDocumentCache(Long documentId) {
        String cacheKey = CACHE_PREFIX + "document:" + documentId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 清理知识库缓存
     */
    private void clearKnowledgeCache(String category) {
        // 清理搜索缓存
        redisTemplate.delete(CACHE_PREFIX + "search:*");

        // 清理分类缓存
        clearCategoryCache();

        if (StrUtil.isNotBlank(category)) {
            String categoryCacheKey = CACHE_PREFIX + "category:" + category;
            redisTemplate.delete(categoryCacheKey);
        }
    }

    /**
     * 清理分类缓存
     */
    private void clearCategoryCache() {
        redisTemplate.delete(CACHE_PREFIX + "categories:tree");
        redisTemplate.delete(CACHE_PREFIX + "categories:all");
    }

    public void vectorizeDocumentAsync(KnowledgeDocument document) {
        CompletableFuture.runAsync(() -> {
            try {
                vectorizeDocument(document);
            } catch (Exception e) {
                log.error("异步向量化文档失败，ID: {}", document.getId(), e);
            }
        });
    }

    public void deleteDocumentVectorAsync(KnowledgeDocument document) {
        CompletableFuture.runAsync(() -> {
            // 需要根据向量ID删除
        });
    }

    /**
     * 向量化文档
     */
    public void vectorizeDocument(KnowledgeDocument document) {
        log.debug("向量化文档，ID: {}, 标题: {}", document.getId(), document.getTitle());
        try {
            // 文档元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", document.getId());
            metadata.put("title", document.getTitle());
            metadata.put("summary", document.getSummary());
            metadata.put("category", document.getCategory());
            metadata.put("tags", document.getTags());
            metadata.put("keywords", document.getKeywords());
            metadata.put("priority", document.getPriority());
            metadata.put("author", document.getAuthor());
            metadata.put("createTime", document.getCreateTime().toString());
            metadata.put("updateTime", document.getUpdateTime().toString());
            metadata.put("source", "knowledge_base");

            // 文档内容
            Document vectorDoc = new Document(document.getContent());
            vectorDoc.getMetadata().putAll(metadata);

            // 保存到向量库
            customerKnowledgeVectorStore.add(List.of(vectorDoc));

            // 更新向量化状态
            document.setIsVectorized(1);
            document.setVectorId(document.getId().toString()); // 使用文档ID作为向量ID
            baseMapper.updateById(document);
            log.debug("文档向量化成功，ID: {}", document.getId());
        } catch (Exception e) {
            log.error("文档向量化失败，ID: {}", document.getId(), e);
            throw new RuntimeException("文档向量化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新分类文档数量
     */
    private void updateCategoryDocumentCount(String category) {
        if (StrUtil.isNotBlank(category)) {
            //int count = knowledgeDocumentMapper.countByCategory(category);
            //knowledgeCategoryMapper.updateDocumentCount(category, count);
        }
    }

    /**
     * 构建搜索缓存键
     */
    private String buildSearchCacheKey(KnowledgeDocumentSearchDto dto) {
        return CACHE_PREFIX + "search:" + dto.hashCode() + ":" + dto.getPage() + ":" + dto.getSize();
    }

    /**
     * 文本搜索（回退方案）
     */
    private List<Map<String, Object>> searchByText(String query) {
        // 实现简单的文本匹配，这里可以扩展为数据库全文搜索
        List<Map<String, Object>> results = new ArrayList<>();

        // 简化实现，实际应该从数据库查询
        if (query.contains("退货")) {
            results.add(createKnowledgeResult("退货政策", "7天无理由退货，商品需保持完好"));
        } else if (query.contains("发货") || query.contains("物流")) {
            results.add(createKnowledgeResult("发货时间", "工作日16:00前下单当天发货"));
        } else if (query.contains("保修")) {
            results.add(createKnowledgeResult("商品保修", "所有商品享受一年保修服务"));
        } else if (query.contains("支付")) {
            results.add(createKnowledgeResult("支付方式", "支持支付宝、微信支付、银联支付"));
        } else {
            results.add(createKnowledgeResult("客服帮助", "您好，请问有什么可以帮您？"));
        }

        return results;
    }

    private Map<String, Object> createKnowledgeResult(String title, String content) {
        Map<String, Object> result = new HashMap<>();
        result.put(TITLE, title);
        result.put(CONTENT, content);
        result.put(SCORE, 0.9);
        return result;
    }

    /**
     * 转换为文档响应格式
     */
    private Map<String, Object> convertToDocumentResponse(KnowledgeDocument document) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", document.getId());
        response.put("title", document.getTitle());
        response.put("content", document.getContent());
        response.put("summary", document.getSummary());
        response.put("category", document.getCategory());
        //response.put("tags", parseJsonToList(document.getTags()));
        //response.put("keywords", parseJsonToList(document.getKeywords()));
        response.put("source", document.getSource());
        response.put("author", document.getAuthor());
        response.put("priority", document.getPriority());
        response.put("status", document.getStatus());
        response.put("view_count", document.getViewCount());
        response.put("useful_count", document.getUsefulCount());
        response.put("useless_count", document.getUselessCount());
        response.put("is_vectorized", document.getIsVectorized());
        response.put("version", document.getVersion());
        response.put("create_time", document.getCreateTime());
        response.put("update_time", document.getUpdateTime());

        if (document.getSimilarityScore() != null) {
            response.put("similarity_score", document.getSimilarityScore());
        }

        return response;
    }

}
