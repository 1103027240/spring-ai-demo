package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.build.CustomerKnowledgeBuild;
import cn.getech.base.demo.dto.*;
import cn.getech.base.demo.entity.KnowledgeDocument;
import cn.getech.base.demo.enums.CursorSortByEnum;
import cn.getech.base.demo.enums.CursorSortDirectionEnum;
import cn.getech.base.demo.enums.KnowledgeDocumentStatusEnum;
import cn.getech.base.demo.mapper.KnowledgeDocumentMapper;
import cn.getech.base.demo.service.KnowledgeCategoryService;
import cn.getech.base.demo.service.KnowledgeDocumentService;
import cn.getech.base.demo.utils.DocumentUtils;
import cn.getech.base.demo.utils.ObjectMapperUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
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
import java.time.LocalDateTime;
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

    @Value("${customer.milvus.search.nprobe:32}")
    private Integer nprobe;

    @Value("${customer.cursor.ttl:300}")
    private String cursorCacheTtl;

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
            return new ArrayList<>();
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
        registerPostCommitAddHook(document);

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
            vectorizeDocumentAsync(document);
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
        // 1. 执行搜索
        SearchResp searchResp = executeVectorSearch(dto);
        if (searchResp == null || CollUtil.isEmpty(searchResp.getSearchResults())) {
            return CursorSearchVO.empty();
        }

        List<KnowledgeDocumentVO> allResults = convertSearchResults(searchResp);

        // 2. 排序
        List<KnowledgeDocumentVO> sortedResults = multiLevelSort(allResults, dto);

        // 3. 双向游标分页
        CursorSearchVO<KnowledgeDocumentVO> result = applyCursorPagination(sortedResults, dto);

        // 4. 设置排序信息
        result.setSortInfoVO(SortInfoVO.builder()
                .primaryField(dto.getSortField())
                .primaryDirection(dto.getSortDirection())
                .secondaryField(dto.getSecondSortField())
                .secondaryDirection(dto.getSecondSortDirection())
                .build());
        return result;
    }

    /**
     * 执行搜索
     */
    public SearchResp executeVectorSearch(KnowledgeDocumentSearchDto dto) {
        float[] embed = embeddingModel.embed(dto.getContent());
        FloatVec floatVec = new FloatVec(embed);

        String filterExpr = customerKnowledgeBuild.buildAdvancedFilterExpression(dto);
        List<String> outputFields = Arrays.asList(DOC_ID, CONTENT, METADATA);

        // 设置搜索参数
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("nprobe", nprobe);
        searchParams.put("range_filter", dto.getThresholdSimilarity());

        // 执行搜索
        return vecMService.search(
                CUSTOMER_COLLECTION_NAME,      // collectionName
                Collections.emptyList(),       // partitionNames
                EMBEDDING,                     // annsField
                dto.getTopK(),                 // topK
                filterExpr,                    // filter
                outputFields,                  // outputFields
                Collections.singletonList(floatVec), // data - 修正：使用 FloatVec
                0L,                            // offset
                (long) dto.getTopK(),          // limit - 应该设置为 topK 的值
                -1,                            // roundDecimal
                searchParams,                  // searchParams
                0L,                            // guaranteeTimestamp
                0L,                            // gracefulTime
                ConsistencyLevel.BOUNDED,      // consistencyLevel
                false                          // ignoreGrowing
        );
    }

    public List<KnowledgeDocumentVO> convertSearchResults(SearchResp searchResp) {
        return searchResp.getSearchResults().get(0).stream().map(hit -> {
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
        if (CollUtil.isEmpty(documents)) {
            return documents;
        }

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
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, false, dto.getPageSize());
        }

        // 1. 首页查询
        if (dto.isFirstPage()) {
            return getFirstPage(sortedResults, dto);
        }

        // 2. 下一页查询
        if (dto.isNextPage()) {
            return getNextPage(sortedResults, dto);
        }

        // 3. 上一页查询
        if (dto.isPrevPage()) {
            return getPrevPage(sortedResults, dto);
        }

        // 默认返回首页
        return getFirstPage(sortedResults, dto);
    }

    /**
     * 获取首页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getFirstPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        int pageSize = dto.getPageSize();
        int totalSize = sortedResults.size();

        // 获取当前页数据
        List<KnowledgeDocumentVO> currentPage = sortedResults.stream()
                .limit(pageSize)
                .collect(Collectors.toList());

        // 计算是否有下一页
        boolean hasNext = totalSize > pageSize;

        // 构建游标
        String prevCursor = null; // 首页没有上一页
        String nextCursor = null;
        if (hasNext && CollUtil.isNotEmpty(currentPage)) {
            KnowledgeDocumentVO lastDoc = currentPage.get(currentPage.size() - 1);
            nextCursor = dto.encodeCursor(
                    lastDoc.getSortKey(dto.getSortField()),
                    lastDoc.getSortKey(dto.getSecondSortField()));
        }
        return CursorSearchVO.success(currentPage, nextCursor, prevCursor, hasNext,false, pageSize);
    }

    /**
     * 获取下一页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getNextPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        String[] cursorValues = dto.decodeCursor(dto.getBackwardCursor());
        String primaryCursor = cursorValues[0];
        String secondCursor = cursorValues[1];

        // 找到游标位置
        int cursorIndex = findCursorIndex(sortedResults, primaryCursor, secondCursor, dto);
        if (cursorIndex < 0) {
            return CursorSearchVO.success(Collections.emptyList(), null, null,false, true,  dto.getPageSize());
        }

        // 获取下一页数据（从游标后一位开始）
        int startIndex = cursorIndex + 1;
        int endIndex = Math.min(startIndex + dto.getPageSize(), sortedResults.size());
        List<KnowledgeDocumentVO> currentPage = sortedResults.subList(startIndex, endIndex);

        // 计算分页信息
        boolean hasPrev = true; // 下一页肯定有上一页
        boolean hasNext = endIndex < sortedResults.size();

        // 构建游标
        String prevCursor = dto.encodeCursor(
                sortedResults.get(startIndex).getSortKey(dto.getSortField()),
                sortedResults.get(startIndex).getSortKey(dto.getSecondSortField()));

        String nextCursor = null;
        if (hasNext && CollUtil.isNotEmpty(currentPage)) {
            KnowledgeDocumentVO lastDoc = currentPage.get(currentPage.size() - 1);
            nextCursor = dto.encodeCursor(
                    lastDoc.getSortKey(dto.getSortField()),
                    lastDoc.getSortKey(dto.getSecondSortField()));
        }
        return CursorSearchVO.success(currentPage, nextCursor, prevCursor, hasNext, hasPrev, dto.getPageSize());
    }

    /**
     * 获取上一页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getPrevPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        String[] cursorValues = dto.decodeCursor(dto.getForwardCursor());
        String primaryCursor = cursorValues[0];
        String secondCursor = cursorValues[1];

        // 找到游标位置
        int cursorIndex = findCursorIndex(sortedResults, primaryCursor, secondCursor, dto);
        if (cursorIndex < 0) {
            return CursorSearchVO.success(Collections.emptyList(), null, null,true, false, dto.getPageSize());
        }

        // 获取上一页数据（从游标前pageSize个开始）
        int startIndex = Math.max(0, cursorIndex - dto.getPageSize());
        int endIndex = cursorIndex;
        List<KnowledgeDocumentVO> currentPage = sortedResults.subList(startIndex, endIndex);

        // 计算分页信息
        boolean hasPrev = startIndex > 0;
        boolean hasNext = true; // 上一页肯定有下一页

        // 构建游标
        String prevCursor = null;
        if (hasPrev && !currentPage.isEmpty()) {
            KnowledgeDocumentVO firstDoc = currentPage.get(0);
            prevCursor = dto.encodeCursor(
                    firstDoc.getSortKey(dto.getSortField()),
                    firstDoc.getSortKey(dto.getSecondSortField()));
        }

        String nextCursor = dto.encodeCursor(
                sortedResults.get(endIndex - 1).getSortKey(dto.getSortField()),
                sortedResults.get(endIndex - 1).getSortKey(dto.getSecondSortField()));
        return CursorSearchVO.success(currentPage, nextCursor, prevCursor, hasNext, hasPrev, dto.getPageSize());
    }

    /**
     * 查找游标位置
     */
    private int findCursorIndex(List<KnowledgeDocumentVO> sortedResults, String primaryCursor, String secondCursor, KnowledgeDocumentSearchDto dto) {
        for (int i = 0; i < sortedResults.size(); i++) {
            KnowledgeDocumentVO doc = sortedResults.get(i);
            String docPrimaryKey = doc.getSortKey(dto.getSortField());
            String docSecondaryKey = doc.getSortKey(dto.getSecondSortField());
            if (docPrimaryKey.equals(primaryCursor) && docSecondaryKey.equals(secondCursor)) {
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
    public void registerPostCommitAddHook(KnowledgeDocument document) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        vectorizeDocumentAsync(document);
                    }
                }
        );
    }

    /**
     * 异步向量化文档 (生产环境推送消息MQ处理)
     */
    public void vectorizeDocumentAsync(KnowledgeDocument document) {
        CompletableFuture.runAsync(() -> {
            try {
                vectorizeDocument(document);
            } catch (Exception e) {
                log.error("异步向量化文档失败，ID: {}", document.getId(), e);
            }
        });
    }

    /**
     * 向量化文档
     */
    public void vectorizeDocument(KnowledgeDocument document) {
        try {
            // 文档元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", document.getTitle());
            metadata.put("summary", document.getSummary());
            metadata.put("categoryId", document.getCategoryId());
            metadata.put("tags", document.getTags());
            metadata.put("keywords", document.getKeywords());
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
