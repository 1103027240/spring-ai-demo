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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
    private Long similarityThreshold;

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
    private ObjectMapperUtils objectMapperUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CustomerKnowledgeBuild customerKnowledgeBuild;

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
    public KnowledgeDocument createDocument(KnowledgeDocumentDto dto) {
        // 校验分类ID是否存在
        knowledgeCategoryService.validateCategoryId(dto.getCategoryId());

        KnowledgeDocument document = customerKnowledgeBuild.buildAddKnowledgeDocument(dto);

        // 1. 保存文档
        baseMapper.insert(document);

        // 2. 文档向量化
        registerPostCommitAddHook(document);

        // 3. 清理缓存
        clearCategoryCache(dto.getCategoryId());

        return document;
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
        boolean contentChanged = false;

        if (StrUtil.isNotBlank(dto.getTitle()) && !dto.getTitle().equals(document.getTitle())) {
            document.setTitle(dto.getTitle());
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getContent()) && !dto.getContent().equals(document.getContent())) {
            document.setContent(dto.getContent());
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getSummary()) && !dto.getSummary().equals(document.getSummary())) {
            document.setSummary(dto.getSummary());
            contentChanged = true;
        }

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(document.getCategoryId())) {
            document.setCategoryId(dto.getCategoryId());
            contentChanged = true;
        }

        if (CollUtil.isNotEmpty(dto.getTags())) {
            document.setTags(objectMapperUtils.convertToJson(dto.getTags()));
            contentChanged = true;
        }

        if (CollUtil.isNotEmpty(dto.getKeywords())) {
            document.setKeywords(objectMapperUtils.convertToJson(dto.getKeywords()));
            contentChanged = true;
        }

        if (StrUtil.isNotBlank(dto.getSource()) && !dto.getSource().equals(document.getSource())) {
            document.setSource(dto.getSource());
            contentChanged = true;
        }

        if (dto.getPriority() != null && !dto.getPriority().equals(document.getPriority())) {
            document.setPriority(dto.getPriority());
            contentChanged = true;
        }

        if (dto.getStatus() != null && !dto.getStatus().equals(document.getStatus())) {
            document.setStatus(dto.getStatus());
            contentChanged = true;
        }

        document.setIsVectorized(2); // 标记为已修改但未向量化
        document.setVersion(document.getVersion() + 1);
        document.setUpdateTime(LocalDateTime.now());

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
        document.setUpdateTime(LocalDateTime.now());
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
        // 生成查询向量
        float[] embed = embeddingModel.embed(dto.getContent());

        // 1. 构建标量过滤条件
        String filterExpr = customerKnowledgeBuild.buildAdvancedFilterExpression(dto);

        // 2. 构建游标过滤条件
        String cursorFilter = customerKnowledgeBuild.buildCursorFilter(dto);

        // 3. 合并所有过滤条件
        String finalFilter = customerKnowledgeBuild.mergeFilters(filterExpr, cursorFilter);

        // 4. 构建排序表达式
        String orderByExpr = customerKnowledgeBuild.buildOrderByExpression(dto);

        // 5. 执行向量搜索
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(CUSTOMER_COLLECTION_NAME)
                .withMetricType(MetricType.COSINE)
                .withVectorFieldName(VECTOR)
                .withVectors(Collections.singletonList(embed))
                .withTopK(dto.getPageSize())
                .withExpr(finalFilter)
                .withOutFields(Arrays.asList(DOC_ID, CONTENT, METADATA))
                .withParams(String.format("{\"nprobe\": %d}", nprobe))
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .build();
        R<SearchResults> searchResult = milvusClient.search(searchParam);
        if (searchResult.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("搜索失败: " + searchResult.getMessage());
        }

        // 6. 处理搜索结果
        SearchResultsWrapper wrapper = new SearchResultsWrapper(searchResult.getData().getResults());
        List<KnowledgeDocumentVO> documents = customerKnowledgeBuild.convertAdvancedSearchResults(wrapper);
        CursorSearchVO<KnowledgeDocumentVO> response = buildAdvancedSearchResponse(documents, dto);
        return response;
    }

    /**
     * 构建响应
     */
    public CursorSearchVO<KnowledgeDocumentVO> buildAdvancedSearchResponse(List<KnowledgeDocumentVO> documents, KnowledgeDocumentSearchDto dto) {
        CursorSearchVO<KnowledgeDocumentVO> response = new CursorSearchVO<>();
        response.setData(documents);
        response.setCurrentSize(documents.size());
        response.setSortBy(dto.getSortBy());
        response.setSortDirection(dto.getSortDirection());

        if (CollUtil.isNotEmpty(documents)) {
            // 构建下一页游标
            KnowledgeDocumentVO lastDoc = documents.get(documents.size() - 1);
            CompositeCursorDto nextCursor = lastDoc.toCursor(dto.getSortBy(), dto.getSortDirection());
            response.setNextCursor(nextCursor.encodeCursor());

            // 构建上一页游标
            KnowledgeDocumentVO firstDoc = documents.get(0);
            CompositeCursorDto prevCursor = firstDoc.toCursor(dto.getSortBy(), dto.getSortDirection());
            response.setPrevCursor(prevCursor.encodeCursor());

            // 检查是否有更多数据
            response.setHasNext(checkHasMoreData(lastDoc, dto));
            response.setHasPrev(checkHasPrevData(firstDoc, dto));

            // 估算总记录数
            response.setTotalSize(estimateTotalSize(dto));
        }

        return response;
    }
    /**
     * 估算总数据量
     */
    private Long estimateTotalSize(KnowledgeDocumentSearchDto dto) {
        try {
            String filterExpr = customerKnowledgeBuild.buildAdvancedFilterExpression(dto);
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(CUSTOMER_COLLECTION_NAME)
                    .withExpr(filterExpr)
                    .withOutFields(Collections.singletonList("id"))
                    .withLimit(1L)
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                    .build();
            R<QueryResults> queryResult = milvusClient.query(queryParam);
            return queryResult.getData().getFieldsDataList().size() > 0 ? 10000L : 0L;
        } catch (Exception e) {
            log.error("估算总数据量失败", e);
            return null;
        }
    }

    /**
     * 检查是否有更多数据
     */
    private boolean checkHasMoreData(KnowledgeDocumentVO lastDoc, KnowledgeDocumentSearchDto dto) {
        try {
            String cacheKey = CUSTOMER_CURSOR_PREFIX + "hasnext:" + lastDoc.getId() + ":" + dto.hashCode();
            Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 构建游标过滤条件
            CompositeCursorDto cursorDto = lastDoc.toCursor(dto.getSortBy(), dto.getSortDirection());
            String cursorFilter = customerKnowledgeBuild.buildCursorFilterForCheck(cursorDto, dto);

            // 合并所有过滤条件
            String filterExpr = customerKnowledgeBuild.buildAdvancedFilterExpression(dto);
            String finalFilter = customerKnowledgeBuild.mergeFilters(filterExpr, cursorFilter);

            // 查询一条数据检查
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(CUSTOMER_COLLECTION_NAME)
                    .withExpr(finalFilter)
                    .withOutFields(Collections.singletonList("id"))
                    .withLimit(1L)
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                    .build();
            R<QueryResults> queryResult = milvusClient.query(queryParam);

            // 缓存
            boolean hasMore = queryResult.getData().getFieldsDataList().size() > 0;
            redisTemplate.opsForValue().set(cacheKey, hasMore, Long.parseLong(cursorCacheTtl), TimeUnit.SECONDS);

            return hasMore;
        } catch (Exception e) {
            log.error("检查是否有更多数据失败", e);
            return false;
        }
    }

    /**
     * 检查是否有上一页数据
     */
    private boolean checkHasPrevData(KnowledgeDocumentVO firstDoc, KnowledgeDocumentSearchDto dto) {
        try {
            String cacheKey = CUSTOMER_CURSOR_PREFIX + "hasprev:" + firstDoc.getId() + ":" + dto.hashCode();
            Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 构建反向游标过滤条件
            CompositeCursorDto cursor = firstDoc.toCursor(dto.getSortBy(), dto.getSortDirection());
            String reversedDirection = CursorSortDirectionEnum.DESC.getId().equals(cursor.getSortDirection()) ? CursorSortDirectionEnum.ASC.getId() : CursorSortDirectionEnum.DESC.getId();
            cursor.setSortDirection(reversedDirection);
            String cursorFilter = customerKnowledgeBuild.buildCursorFilterForCheck(cursor, dto);

            // 合并所有过滤条件
            String filterExpr = customerKnowledgeBuild.buildAdvancedFilterExpression(dto);
            String finalFilter = customerKnowledgeBuild.mergeFilters(filterExpr, cursorFilter);

            // 查询一条数据检查
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(CUSTOMER_COLLECTION_NAME)
                    .withExpr(finalFilter)
                    .withOutFields(Collections.singletonList("id"))
                    .withLimit(1L)
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                    .build();
            R<QueryResults> queryResult = milvusClient.query(queryParam);

            // 缓存
            boolean hasMore = queryResult.getData().getFieldsDataList().size() > 0;
            redisTemplate.opsForValue().set(cacheKey, hasMore, Long.parseLong(cursorCacheTtl), TimeUnit.SECONDS);

            return hasMore;
        } catch (Exception e) {
            log.error("检查是否有上一页数据失败", e);
            return false;
        }
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
            metadata.put("createTime", document.getCreateTime().toString());

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
