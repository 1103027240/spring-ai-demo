package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.CustomerKnowledgeBuild;
import cn.example.base.demo.build.KnowledgeDocumentBuild;
import cn.example.base.demo.check.CustomerKnowledgeCheck;
import cn.example.base.demo.dto.*;
import cn.example.base.demo.entity.KnowledgeDocument;
import cn.example.base.demo.enums.CursorSortByEnum;
import cn.example.base.demo.enums.KnowledgeDocumentStatusEnum;
import cn.example.base.demo.mapper.KnowledgeDocumentMapper;
import cn.example.base.demo.service.KnowledgeCategoryService;
import cn.example.base.demo.service.KnowledgeDocumentService;
import cn.example.base.demo.utils.DocumentUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.constant.FieldValueConstant.*;
import static cn.example.base.demo.constant.RedisKeyConstant.*;
import static cn.example.base.demo.enums.SortDirectionEnum.DESC;

/**
 * @author 11030
 */
@Slf4j
@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> implements KnowledgeDocumentService {

    @Value("${customer.knowledge.similarity-threshold:0.7}")
    private Double similarityThreshold;

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

    @Autowired
    private KnowledgeDocumentBuild knowledgeDocumentBuild;

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
        knowledgeCategoryService.validateCategoryId(dto.getCategoryId());

        // 保存文档
        KnowledgeDocument document = knowledgeDocumentBuild.buildAddKnowledgeDocument(dto);
        baseMapper.insert(document);

        // 文档向量化
        registerPostCommitAddHook(document, dto);

        // 删除缓存
        clearCategoryCache(dto.getCategoryId());
    }

    @Transactional
    @Override
    public void batchCreateDocument(KnowledgeDocumentAddDto dto) {// 更新文档
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
        if (dto.getId() == null) {
            throw new RuntimeException("文档ID不能为空");
        }

        KnowledgeDocument document = baseMapper.selectById(dto.getId());
        if (document == null) {
            throw new RuntimeException("文档ID不存在" + dto.getId());
        }

        // 修改文档
        Long oldCategoryId = document.getCategoryId();
        boolean contentChanged = knowledgeDocumentBuild.buildUpdateKnowledgeDocument(document, dto);
        baseMapper.updateById(document);

        // 文档向量化修改
        if (contentChanged) {
            vectorizeDocumentAsync(document, dto);
        }

        // 删除缓存
        clearDocumentCache(document.getId());
        clearCategoryCache(oldCategoryId);
        clearCategoryCache(document.getCategoryId());
    }

    @Transactional
    @Override
    public void deleteDocument(Long documentId) {
        KnowledgeDocument document = baseMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档ID不存在" + documentId);
        }

        // 修改文档状态
        document.setStatus(KnowledgeDocumentStatusEnum.DELETED.getId());
        document.setUpdateTime(System.currentTimeMillis());
        baseMapper.updateById(document);

        // 删除文档向量化
        deleteDocumentVectorAsync(document);

        // 删除缓存
        clearDocumentCache(documentId);
        clearCategoryCache(document.getCategoryId());
    }

    @Override
    public KnowledgeDocument getDocument(Long documentId) {
        String cacheKey = CUSTOMER_KNOWLEDGE_PREFIX + "documentId:" + documentId;
        KnowledgeDocument cachedDocument = (KnowledgeDocument) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDocument != null) {
            return cachedDocument;
        }

        KnowledgeDocument document = baseMapper.selectById(documentId);
        if (document != null && document.getStatus() != KnowledgeDocumentStatusEnum.DELETED.getId()) {
            redisTemplate.opsForValue().set(cacheKey, document, CACHE_TTL, TimeUnit.SECONDS);
        }
        return document;
    }

    /**
     * 搜索知识库文档（向量 + 标量混合查询、游标无限分页）
     * - 游标无限分页：topK 动态计算
     * - 混合查询：向量相似度 + 标量过滤
     */
    @Override
    public CursorSearchVO<KnowledgeDocumentVO> search(KnowledgeDocumentSearchDto dto) {
        // 校验参数
        if (StrUtil.isBlank(dto.getContent())) {
            return CursorSearchVO.empty();
        }

        customerKnowledgeCheck.validateSearchParams(dto);

        // 搜索并排序
        List<KnowledgeDocumentVO> sortedResults = doSearchAndSort(dto);
        if (CollUtil.isEmpty(sortedResults)) {
            return CursorSearchVO.empty();
        }

        // 封装返回结果
        CursorSearchVO<KnowledgeDocumentVO> result = applyCursorPagination(sortedResults, dto);

        // 封装其他信息
        result.setSortInfoVO(SortInfoVO.builder()
                .primaryField(dto.getSortField())
                .primaryDirection(dto.getSortDirection())
                .secondField(dto.getSecondSortField())
                .secondDirection(dto.getSecondSortDirection())
                .build());
        return result;
    }

    /**
     * 执行搜索并排序
     * 1. 向量搜索（content 字段向量化） + 标量过滤
     * 2. 结果转换
     * 3. 多级排序
     */
    private List<KnowledgeDocumentVO> doSearchAndSort(KnowledgeDocumentSearchDto dto) {
        // 执行向量搜索 + 标量过滤
        SearchResp searchResp = executeVectorSearch(dto);
        if (searchResp == null || CollUtil.isEmpty(searchResp.getSearchResults())) {
            return Collections.emptyList();
        }

        // 转换搜索结果
        List<KnowledgeDocumentVO> results = customerKnowledgeBuild.convertSearchResults(searchResp);

        // 多级排序
        return multiLevelSort(results, dto);
    }

    /**
     * 执行向量搜索（向量 + 标量混合查询、无限分页）
     */
    public SearchResp executeVectorSearch(KnowledgeDocumentSearchDto dto) {
        try {
            // 向量化查询内容
            float[] embed = embeddingModel.embed(dto.getContent());
            FloatVec floatVec = new FloatVec(embed);

            // 构建搜索参数
            Map<String, Object> searchParams = customerKnowledgeBuild.buildSearchParams(dto);

            // 计算动态 topK 值
            int dynamicTopK = customerKnowledgeBuild.calculateDynamicTopK(dto);

            // 执行向量搜索 + 标量过滤
            return vecMService.search(
                    CUSTOMER_COLLECTION_NAME,
                    Collections.emptyList(),
                    EMBEDDING,
                    dynamicTopK,
                    customerKnowledgeBuild.buildAdvancedFilterExpression(dto),
                    Arrays.asList(DOC_ID, CONTENT, METADATA),
                    Collections.singletonList(floatVec),
                    0L,
                    (long) dynamicTopK,
                    -1,
                    searchParams,
                    0L,
                    0L,
                    ConsistencyLevel.BOUNDED,
                    false);
        } catch (Exception e) {
            log.error("向量搜索异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 多级排序
     */
    private List<KnowledgeDocumentVO> multiLevelSort(List<KnowledgeDocumentVO> documents, KnowledgeDocumentSearchDto dto) {
        Comparator<KnowledgeDocumentVO> comparator = buildComparatorChain(dto);
        return documents.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 构建比较器链
     */
    private Comparator<KnowledgeDocumentVO> buildComparatorChain(KnowledgeDocumentSearchDto dto) {
        Comparator<KnowledgeDocumentVO> comparator = createComparator(dto.getSortField(), dto.getSortDirection());
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

        CursorSortByEnum cursorSortByEnum = CursorSortByEnum.getCursorSort(field);
        switch (field) {
            case SCORE:
                comparator = Comparator.comparing(KnowledgeDocumentVO::getScore, Comparator.nullsLast(Float::compareTo));
                break;
            case CREATE_TIME:
                comparator = Comparator.comparing(KnowledgeDocumentVO::getCreateTime, Comparator.nullsLast(Long::compareTo));
                break;
            case DOC_ID:
            default:
                comparator = Comparator.comparing(KnowledgeDocumentVO::getDocId, Comparator.nullsLast(Long::compareTo));
                break;
        }

        if (DESC.getId().equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    /**
     * 应用双向游标分页
     */
    public CursorSearchVO<KnowledgeDocumentVO> applyCursorPagination(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
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
     * 获取首页数据（页码0）
     */
    private CursorSearchVO<KnowledgeDocumentVO> getFirstPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        int pageSize = dto.getPageSize();
        List<KnowledgeDocumentVO> currentPageList = sortedResults.stream()
                .limit(pageSize)
                .collect(Collectors.toList());

        boolean hasNext = sortedResults.size() > pageSize;

        // nextCursor: 当前页最后一条数据，页码为0
        String nextCursor = hasNext
                ? customerKnowledgeBuild.buildCursor(dto, currentPageList.get(currentPageList.size() - 1), 0)
                : null;

        return CursorSearchVO.success(currentPageList, nextCursor, null, hasNext, false, pageSize);
    }

    /**
     * 获取下一页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getNextPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        if (StrUtil.isBlank(dto.getNextCursor())) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, true, dto.getPageSize());
        }

        String[] cursorValues = dto.decodeCursor(dto.getNextCursor());
        int currentPage = Integer.parseInt(cursorValues[0]);

        int cursorIndex = customerKnowledgeBuild.findCursorIndex(sortedResults, cursorValues[1], cursorValues[2], dto);
        if (cursorIndex < 0 || cursorIndex >= sortedResults.size()) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, false, dto.getPageSize());
        }

        return buildPagedResult(sortedResults, dto, cursorIndex, currentPage, true);
    }

    /**
     * 获取上一页数据
     */
    private CursorSearchVO<KnowledgeDocumentVO> getPrevPage(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto) {
        if (StrUtil.isBlank(dto.getPrevCursor())) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, true, false, dto.getPageSize());
        }

        String[] cursorValues = dto.decodeCursor(dto.getPrevCursor());
        int currentPage = Integer.parseInt(cursorValues[0]);

        int cursorIndex = customerKnowledgeBuild.findCursorIndex(sortedResults, cursorValues[1], cursorValues[2], dto);
        if (cursorIndex < 0 || cursorIndex >= sortedResults.size()) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, false, dto.getPageSize());
        }

        return buildPagedResult(sortedResults, dto, cursorIndex, currentPage, false);
    }

    /**
     * 构建分页结果
     * - nextCursor: 当前页最后一条数据的位置（当前页码）
     * - prevCursor: 当前页第一条数据的前一页位置（当前页码-1）
     */
    private CursorSearchVO<KnowledgeDocumentVO> buildPagedResult(List<KnowledgeDocumentVO> sortedResults, KnowledgeDocumentSearchDto dto,
                                                                 int cursorIndex, int currentPage, boolean isNext) {
        int startIndex, endIndex;
        if (isNext) {
            // 下一页：从游标位置+1开始（游标是上一页最后一条）
            startIndex = cursorIndex + 1;
            endIndex = Math.min(startIndex + dto.getPageSize(), sortedResults.size());
        } else {
            // 上一页：从游标位置往前推pageSize条（游标是下一页第一条）
            startIndex = Math.max(0, cursorIndex - dto.getPageSize());
            endIndex = cursorIndex;
        }

        if (startIndex >= endIndex || startIndex >= sortedResults.size() || endIndex > sortedResults.size()) {
            return CursorSearchVO.success(Collections.emptyList(), null, null, false, false, dto.getPageSize());
        }

        List<KnowledgeDocumentVO> currentPageList = sortedResults.subList(startIndex, endIndex);
        boolean hasPrev = startIndex > 0;
        boolean hasNext = endIndex < sortedResults.size();

        // 游标页码为当前页的实际页码（下一页时需要+1）
        int actualPageNum = isNext ? currentPage + 1 : currentPage;

        // nextCursor: 当前页最后一条数据（用于"下一页"）
        // prevCursor: 当前页第一条数据（用于"上一页"，从它往前推pageSize条）
        String nextCursor = hasNext
                ? customerKnowledgeBuild.buildCursor(dto, sortedResults.get(endIndex - 1), actualPageNum)
                : null;
        String prevCursor = hasPrev
                ? customerKnowledgeBuild.buildCursor(dto, sortedResults.get(startIndex), actualPageNum)
                : null;

        return CursorSearchVO.success(currentPageList, nextCursor, prevCursor, hasNext, hasPrev, dto.getPageSize());
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
                });
    }

    /**
     * 异步向量化文档
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
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", document.getTitle());
            metadata.put("summary", document.getSummary());
            metadata.put("categoryId", document.getCategoryId());
            metadata.put("tags", new Gson().toJsonTree(dto.getTags()));
            metadata.put("keywords", new Gson().toJsonTree(dto.getKeywords()));
            metadata.put("author", document.getAuthor());
            metadata.put("source", document.getSource());
            metadata.put("priority", document.getPriority());
            metadata.put("status", document.getStatus());
            metadata.put("createTime", document.getCreateTime());

            Document vectorDoc = DocumentUtils.createDocument(document.getId().toString(), document.getContent(), metadata);
            customerKnowledgeVectorStore.add(List.of(vectorDoc));

            document.setIsVectorized(1);
            document.setVectorId(document.getId().toString());
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
