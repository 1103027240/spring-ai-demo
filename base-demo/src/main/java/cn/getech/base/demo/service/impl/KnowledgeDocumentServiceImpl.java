package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.CustomerKnowledgeBuild;
import cn.getech.base.demo.check.CustomerKnowledgeCheck;
import cn.getech.base.demo.dto.*;
import cn.getech.base.demo.entity.KnowledgeDocument;
import cn.getech.base.demo.enums.KnowledgeDocumentStatusEnum;
import cn.getech.base.demo.mapper.KnowledgeDocumentMapper;
import cn.getech.base.demo.service.KnowledgeCategoryService;
import cn.getech.base.demo.service.KnowledgeDocumentService;
import cn.getech.base.demo.utils.DocumentUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import io.milvus.client.MilvusServiceClient;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.plus.service.IVecMService;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static cn.getech.base.demo.constant.FieldConstant.*;
import static cn.getech.base.demo.constant.FieldValueConstant.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> implements KnowledgeDocumentService {

    @Value("${customer.knowledge.similarity-threshold:0.7}")
    private Double similarityThreshold;

    @Value("${customer.milvus.search.nprobe:128}")
    private Integer nprobe;

    @Value("${customer.cursor.ttl:300}")
    private Long cursorCacheTtl; // 游标缓存时间（秒），未来可用于游标缓存优化

    @Value("${customer.search.redundancy.enabled:true}")
    private Boolean enableSearchRedundancy; // 是否启用搜索冗余（提高游标命中率）

    @Value("${customer.search.redundancy.factor:1.2}")
    private Double searchRedundancyFactor; // 搜索冗余因子（1.2表示多查20%）

    @Value("${customer.search.redundancy.maxExtra:100}")
    private Integer searchRedundancyMaxExtra; // 搜索冗余最大额外条数

    @Autowired
    private EmbeddingModel embeddingModel;

    @Resource(name = "customerKnowledgeVectorStore")
    private VectorStore customerKnowledgeVectorStore;

    @Autowired
    private MilvusServiceClient milvusClient;

    @Autowired
    private KnowledgeCategoryService knowledgeCategoryService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CustomerKnowledgeBuild customerKnowledgeBuild;

    @Autowired
    private IVecMService vecMService;

    @Autowired
    private CustomerKnowledgeCheck customerKnowledgeCheck;

    /**
     * 搜索相关知识
     */
    @Override
    public List<Map<String, Object>> searchKnowledge(String query, int limit) {
        try {
            if (StrUtil.isBlank(query)) {
                log.warn("搜索知识失败：查询内容为空");
                return Collections.emptyList();
            }

            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(limit)
                    .similarityThreshold(similarityThreshold)
                    .build();

            List<Document> documents = customerKnowledgeVectorStore.similaritySearch(request);

            if (CollUtil.isEmpty(documents)) {
                log.debug("知识搜索无结果: query={}, threshold={}", query, similarityThreshold);
                return Collections.emptyList();
            }

            return documents.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>(doc.getMetadata());
                        result.put(CONTENT, doc.getText());
                        return result;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索知识失败: query={}, limit={}, error={}", query, limit, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Transactional
    @Override
    public void createDocument(KnowledgeDocumentDto dto) {
        // 校验分类ID是否存在
        knowledgeCategoryService.validateCategoryId(dto.getCategoryId());

        KnowledgeDocument document = customerKnowledgeBuild.buildAddKnowledgeDocument(dto);

        // 1. 保存文档
        baseMapper.insert(document);

        // 2. 文档向量化
        registerPostCommitAddHook(document, dto);

        // 3. 清理缓存
        clearCategoryCache(dto.getCategoryId());
    }

    @Transactional
    @Override
    public void batchCreateDocuments(KnowledgeDocumentAddDto dto) {// 更新文档
        for (KnowledgeDocumentDto req : dto.getDocuments()) {
            try {
                createDocument(req);
            } catch (Exception e) {
                log.error("批量创建文档失败，标题: {}", req.getTitle(), e);
            }
        }
    }

    @Transactional
    @Override
    public void updateDocument(KnowledgeDocumentDto dto) {
        // 校验文档
        if (dto.getId() == null) {
            throw new RuntimeException("文档ID不能为空");
        }

        KnowledgeDocument document = baseMapper.selectById(dto.getId());
        if (document == null) {
            throw new RuntimeException("文档ID不存在" + dto.getId());
        }

        Long oldCategoryId = document.getCategoryId();
        boolean contentChanged = customerKnowledgeBuild.buildUpdateKnowledgeDocument(document, dto);

        // 1. 更新文档
        baseMapper.updateById(document);

        // 2. 如果内容有变化，重新向量化
        if (contentChanged) {
            vectorizeDocumentAsync(document, dto);
        }

        // 3. 清理缓存
        clearDocumentCache(document.getId());
        clearCategoryCache(oldCategoryId);
        clearCategoryCache(document.getCategoryId());
    }

    @Transactional
    @Override
    public void deleteDocument(Long documentId) {
        // 校验文档
        KnowledgeDocument document = baseMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档ID不存在" + documentId);
        }

        // 1. 逻辑删除
        document.setStatus(KnowledgeDocumentStatusEnum.DELETED.getId());
        document.setUpdateTime(System.currentTimeMillis());
        baseMapper.updateById(document);

        // 2. 异步删除向量
        deleteDocumentVectorAsync(document);

        // 3. 清理缓存
        clearDocumentCache(documentId);
        clearCategoryCache(document.getCategoryId());
    }

    @Override
    public KnowledgeDocument getDocumentById(Long documentId) {
        // 先从缓存获取
        String cacheKey = CUSTOMER_KNOWLEDGE_PREFIX + "documentId:" + documentId;
        KnowledgeDocument cachedDocument = (KnowledgeDocument) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDocument != null) {
            return cachedDocument;
        }

        // 再从数据库获取
        KnowledgeDocument document = baseMapper.selectById(documentId);
        if (document != null && document.getStatus() != KnowledgeDocumentStatusEnum.DELETED.getId()) { // 非删除状态
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, document, CACHE_TTL, TimeUnit.SECONDS);
        }
        return document;
    }

    @Override
    public CursorSearchVO<KnowledgeDocumentVO> search(KnowledgeDocumentSearchDto dto) {
        try {
            if (StrUtil.isBlank(dto.getContent())) {
                return CursorSearchVO.empty();
            }

            // 1.校验参数
            customerKnowledgeCheck.validateSearchParams(dto);
            dto.setTopK(dto.calculateTopK());

            // 2.执行搜索并重排
            List<KnowledgeDocumentVO> sortedResults = doSearchAndSort(dto);
            if (CollUtil.isEmpty(sortedResults)) {
                return CursorSearchVO.empty();
            }

            // 3.封装返回结果
            CursorSearchVO<KnowledgeDocumentVO> result = applyCursorPagination(sortedResults, dto);

            // 4.封装其他信息
            result.setSortInfoVO(SortInfoVO.builder()
                    .primaryField(dto.getSortField())
                    .primaryDirection(dto.getSortDirection())
                    .secondField(dto.getSecondSortField())
                    .secondDirection(dto.getSecondSortDirection())
                    .build());
            return result;
        } catch (Exception e) {
            log.error("搜索异常: content={}, error={}", dto.getContent(), e.getMessage(), e);
            return CursorSearchVO.empty();
        }
    }

    /**
     * 执行搜索并排序
     */
    private List<KnowledgeDocumentVO> doSearchAndSort(KnowledgeDocumentSearchDto dto) {
        SearchResp searchResp = executeVectorSearch(dto);
        if (searchResp == null || CollUtil.isEmpty(searchResp.getSearchResults())) {
            return Collections.emptyList();
        }

        List<KnowledgeDocumentVO> results = convertSearchResults(searchResp);
        return multiLevelSort(results, dto);
    }

    /**
     * 执行向量搜索
     */
    public SearchResp executeVectorSearch(KnowledgeDocumentSearchDto dto) {
        try {
            float[] embed = embeddingModel.embed(dto.getContent());
            FloatVec floatVec = new FloatVec(embed);

            Map<String, Object> searchParams = buildSearchParams(dto);

            return vecMService.search(
                    CUSTOMER_COLLECTION_NAME,
                    Collections.emptyList(),
                    EMBEDDING,
                    calculateEffectiveTopK(dto),
                    customerKnowledgeBuild.buildAdvancedFilterExpression(dto),
                    Arrays.asList(DOC_ID, CONTENT, METADATA),
                    Collections.singletonList(floatVec),
                    0L,
                    (long) calculateEffectiveTopK(dto),
                    -1,
                    searchParams,
                    0L,
                    0L,
                    ConsistencyLevel.BOUNDED,
                    false
            );
        } catch (Exception e) {
            log.error("向量搜索异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建搜索参数
     */
    private Map<String, Object> buildSearchParams(KnowledgeDocumentSearchDto dto) {
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("nprobe", nprobe);
        searchParams.put("metric_type", "COSINE");
        searchParams.put("radius", 1.0 - dto.getThresholdSimilarity());
        searchParams.put("range_filter", 1.0);
        return searchParams;
    }

    /**
     * 计算有效的topK（考虑冗余查询）
     */
    private int calculateEffectiveTopK(KnowledgeDocumentSearchDto dto) {
        int effectiveTopK = dto.getTopK();
        if (enableSearchRedundancy && !dto.isFirstPage()) {
            int requestedTopK = dto.getTopK();
            effectiveTopK = (int) Math.ceil(requestedTopK * searchRedundancyFactor);
            effectiveTopK = Math.min(effectiveTopK, requestedTopK + searchRedundancyMaxExtra);
        }
        return effectiveTopK;
    }

    public List<KnowledgeDocumentVO> convertSearchResults(SearchResp searchResp) {
        return searchResp.getSearchResults().get(0).stream()
                .map(hit -> {
                    KnowledgeDocumentVO doc = new KnowledgeDocumentVO();
                    doc.setDocId(Long.parseLong((String) hit.getId()));
                    doc.setScore(hit.getScore());

                    Map<String, Object> entityData = hit.getEntity();
                    if (entityData != null) {
                        doc.setContent((String) entityData.get(CONTENT));

                        Object metadataObj = entityData.get(METADATA);
                        if (metadataObj != null) {
                            Map<String, Object> metadata = JSONUtil.toBean(metadataObj.toString(), Map.class);
                            doc.setMetadata(metadata);

                            Object createTimeObj = metadata.get(CREATE_TIME);
                            if (createTimeObj != null) {
                                doc.setCreateTime((Long) createTimeObj);
                            }
                        }
                    }
                    return doc;
                }).collect(Collectors.toList());
    }

    /**
     * 排序
     */
    private List<KnowledgeDocumentVO> multiLevelSort(List<KnowledgeDocumentVO> documents, KnowledgeDocumentSearchDto dto) {
        // 创建比较器链
        Comparator<KnowledgeDocumentVO> comparator = buildComparatorChain(dto);

        // 执行排序
        return documents.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 构建比较器链
     */
    private Comparator<KnowledgeDocumentVO> buildComparatorChain(KnowledgeDocumentSearchDto dto) {
        Comparator<KnowledgeDocumentVO> comparator = null;

        // 1. 主排序
        comparator = createComparator(dto.getSortField(), dto.getSortDirection());

        // 2. 次排序（主排序相同时使用）
        if (StrUtil.isNotBlank(dto.getSecondSortField())) {
            Comparator<KnowledgeDocumentVO> secondaryComparator = createComparator(dto.getSecondSortField(), dto.getSecondSortDirection());
            comparator = comparator.thenComparing(secondaryComparator);
        }

        return comparator;
    }

    /**
     * 创建字段比较器
     */
    private Comparator<KnowledgeDocumentVO> createComparator(String field, String direction) {
        Comparator<KnowledgeDocumentVO> comparator = null;

        switch (field) {
            case "score":
                comparator = Comparator.comparing(KnowledgeDocumentVO::getScore, Comparator.nullsLast(Float::compareTo));
                break;
            case "createTime":
                comparator = Comparator.comparing(KnowledgeDocumentVO::getCreateTime, Comparator.nullsLast(Long::compareTo));
                break;
            case "docId":
            default:
                comparator = Comparator.comparing(KnowledgeDocumentVO::getDocId, Comparator.nullsLast(Long::compareTo));
                break;
        }

        // 处理排序方向
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    /**
     * 应用双向游标分页
     */
    public CursorSearchVO<KnowledgeDocumentVO> applyCursorPagination(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        if (CollUtil.isEmpty(sortedResults)) {
            return CursorSearchVO.empty();
        }

        if (dto.isFirstPage()) {
            return getFirstPage(sortedResults, dto);
        }
        if (dto.isNextPage()) {
            return getNextPage(sortedResults, dto);
        }
        if (dto.isPrevPage()) {
            return getPrevPage(sortedResults, dto);
        }

        return getFirstPage(sortedResults, dto);
    }

    /**
     * 获取首页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getFirstPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        int pageSize = dto.getPageSize();
        List<KnowledgeDocumentVO> currentPage = sortedResults.stream()
                .limit(pageSize)
                .collect(Collectors.toList());

        boolean hasNext = sortedResults.size() > pageSize;
        String prevCursor = null;
        String nextCursor = hasNext && CollUtil.isNotEmpty(currentPage)
                ? buildCursor(dto, sortedResults.get(pageSize), 1)
                : null;

        return CursorSearchVO.success(currentPage, nextCursor, prevCursor, hasNext, false, pageSize);
    }

    /**
     * 获取下一页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getNextPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        if (StrUtil.isBlank(dto.getBackwardCursor())) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, true, dto.getPageSize());
        }

        try {
            String[] cursorValues = dto.decodeCursor(dto.getBackwardCursor());
            int currentPageNum = Integer.parseInt(cursorValues[0]);

            int cursorIndex = findCursorIndex(sortedResults, cursorValues[1], cursorValues[2], dto);
            if (cursorIndex < 0) {
                return CursorSearchVO.success(Collections.emptyList(), null, null, false, true, dto.getPageSize());
            }

            return buildPagedResult(sortedResults, dto, cursorIndex, currentPageNum, true);
        } catch (Exception e) {
            log.error("获取下一页异常", e);
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, true, dto.getPageSize());
        }
    }

    /**
     * 获取上一页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getPrevPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        if (StrUtil.isBlank(dto.getForwardCursor())) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, true, false, dto.getPageSize());
        }

        try {
            String[] cursorValues = dto.decodeCursor(dto.getForwardCursor());
            int currentPageNum = Integer.parseInt(cursorValues[0]);

            int cursorIndex = findCursorIndex(sortedResults, cursorValues[1], cursorValues[2], dto);
            if (cursorIndex < 0) {
                return CursorSearchVO.success(Collections.emptyList(), null, null, true, false, dto.getPageSize());
            }

            return buildPagedResult(sortedResults, dto, cursorIndex, currentPageNum, false);
        } catch (Exception e) {
            log.error("获取上一页异常", e);
            return CursorSearchVO.success(Collections.emptyList(), null, null, true, false, dto.getPageSize());
        }
    }

    /**
     * 构建分页结果
     */
    private CursorSearchVO<KnowledgeDocumentVO> buildPagedResult(List<KnowledgeDocumentVO> sortedResults,
                                                              KnowledgeDocumentSearchDto dto,
                                                              int cursorIndex,
                                                              int currentPageNum,
                                                              boolean isNext) {
        int startIndex, endIndex;
        if (isNext) {
            startIndex = cursorIndex;
            endIndex = Math.min(startIndex + dto.getPageSize(), sortedResults.size());
        } else {
            startIndex = Math.max(0, cursorIndex - dto.getPageSize());
            endIndex = cursorIndex;
        }

        // 检查数据完整性
        if (startIndex >= endIndex || (endIndex - startIndex) < dto.getPageSize()) {
            return isNext
                    ? CursorSearchVO.success(Collections.emptyList(), null, null, false, true, dto.getPageSize())
                    : CursorSearchVO.success(Collections.emptyList(), null, null, false, false, dto.getPageSize());
        }

        List<KnowledgeDocumentVO> currentPage = sortedResults.subList(startIndex, endIndex);
        boolean hasPrev = startIndex > 0;
        boolean hasNext = endIndex < sortedResults.size();

        String prevCursor = hasPrev && !currentPage.isEmpty()
                ? buildCursor(dto, sortedResults.get(startIndex), currentPageNum - 1)
                : null;
        String nextCursor = hasNext && !currentPage.isEmpty()
                ? buildCursor(dto, sortedResults.get(endIndex), currentPageNum + 1)
                : null;

        return CursorSearchVO.success(currentPage, nextCursor, prevCursor, hasNext, hasPrev, dto.getPageSize());
    }

    /**
     * 构建游标
     */
    private String buildCursor(KnowledgeDocumentSearchDto dto, KnowledgeDocumentVO doc, int pageNum) {
        return dto.encodeCursor(
                pageNum,
                doc.getSortKey(dto.getSortField()),
                doc.getSortKey(dto.getSecondSortField())
        );
    }

    /**
     * 查找游标位置（精确匹配）
     */
    private int findCursorIndex(List<KnowledgeDocumentVO> sortedResults, String primaryCursor, String secondCursor, KnowledgeDocumentSearchDto dto) {
        if (CollUtil.isEmpty(sortedResults)) {
            return -1;
        }

        for (int i = 0; i < sortedResults.size(); i++) {
            KnowledgeDocumentVO doc = sortedResults.get(i);
            if (doc.getSortKey(dto.getSortField()).equals(primaryCursor)
                    && doc.getSortKey(dto.getSecondSortField()).equals(secondCursor)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 清理文档缓存
     */
    private void clearDocumentCache(Long documentId) {
        String cacheKey = CUSTOMER_KNOWLEDGE_PREFIX + "documentId:" + documentId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 清理分类缓存
     */
    private void clearCategoryCache(Long categoryId) {
        if (Objects.nonNull(categoryId)) {
            redisTemplate.delete(CUSTOMER_KNOWLEDGE_PREFIX + "categoryId:" + categoryId);
            redisTemplate.delete(CUSTOMER_KNOWLEDGE_PREFIX + "categories:tree");
            redisTemplate.delete(CUSTOMER_KNOWLEDGE_PREFIX + "categories:all");
        }
    }

    /**
     * 注册事务后钩子
     */
    public void registerPostCommitAddHook(KnowledgeDocument document, KnowledgeDocumentDto dto) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        vectorizeDocumentAsync(document, dto);
                    }
                }
        );
    }

    /**
     * 异步向量化文档 (生产环境推送消息MQ处理)
     */
    public void vectorizeDocumentAsync(KnowledgeDocument document, KnowledgeDocumentDto dto) {
        CompletableFuture.runAsync(() -> {
            try {
                vectorizeDocument(document, dto);
            } catch (Exception e) {
                log.error("异步向量化文档失败，ID: {}", document.getId(), e);
            }
        });
    }

    /**
     * 向量化文档
     */
    public void vectorizeDocument(KnowledgeDocument document, KnowledgeDocumentDto dto) {
        try {
            // 文档元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", document.getTitle());
            metadata.put("summary", document.getSummary());
            metadata.put("categoryId", document.getCategoryId());
            metadata.put("tags", new Gson().toJsonTree(document.getTags())); //不能转成数组字符串
            metadata.put("keywords", new Gson().toJsonTree(dto.getKeywords()));
            metadata.put("author", document.getAuthor());
            metadata.put("source", document.getSource());
            metadata.put("priority", document.getPriority());
            metadata.put("status", document.getStatus());
            metadata.put("createTime", document.getCreateTime());

            // 文档内容
            Document vectorDoc = DocumentUtils.createDocument(document.getId().toString(), document.getContent(), metadata);

            // 保存到向量库
            customerKnowledgeVectorStore.add(List.of(vectorDoc));

            // 更新向量化状态
            document.setIsVectorized(1);
            document.setVectorId(document.getId().toString()); // 使用文档ID作为向量ID
            baseMapper.updateById(document);
        } catch (Exception e) {
            log.error("文档向量化失败，ID: {}", document.getId(), e);
            throw new RuntimeException("文档向量化失败: " + e.getMessage(), e);
        }
    }

    public void deleteDocumentVectorAsync(KnowledgeDocument document) {
        CompletableFuture.runAsync(() -> {
            customerKnowledgeVectorStore.delete(List.of(document.getId().toString()));
        });
    }


}
