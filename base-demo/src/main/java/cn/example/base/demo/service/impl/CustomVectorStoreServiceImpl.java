package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.CustomVectorStoreService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.milvus.client.MilvusServiceClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @author 11030
 */
@Slf4j
@Service
public class CustomVectorStoreServiceImpl implements CustomVectorStoreService {

    @Autowired
    private MilvusServiceClient milvusClient;

    @Resource(name = "ragDocumentVectorStore")
    private VectorStore ragDocumentVectorStore;

    // 操作是milvus数据库中long_term_memory集合
    @Value("${spring.ai.vectorstore.milvus.collection-name}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.embedding-dimension}")
    private int embeddingDimension;

    /**
     * 创建集合
     */
//    @Override
//    public void createCollection() {
//        try {
//            // 检查集合是否存在
//            R<Boolean> resp = milvusClient.hasCollection(
//                    HasCollectionParam.newBuilder()
//                            .withCollectionName(collectionName)
//                            .build()
//            );
//            if (resp.getData() != null && resp.getData()) {
//                log.info("Collection {} already exists", collectionName);
//                return;
//            }
//
//            // 创建字段
//            List<FieldType> fields = Arrays.asList(
//                    FieldType.newBuilder()
//                            .withName("doc_id")
//                            .withDataType(DataType.VarChar)
//                            .withMaxLength(255)
//                            .withPrimaryKey(true)
//                            .build(),
//                    FieldType.newBuilder()
//                            .withName("content")
//                            .withDataType(DataType.VarChar)
//                            .withMaxLength(65535)
//                            .build(),
//                    FieldType.newBuilder()
//                            .withName("embedding")
//                            .withDataType(DataType.FloatVector)
//                            .withDimension(embeddingDimension)
//                            .build(),
//                    FieldType.newBuilder()
//                            .withName("metadata")
//                            .withDataType(DataType.JSON)
//                            .build()
//            );
//
//            // 创建集合
//            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
//                    .withCollectionName(collectionName)
//                    .withFieldTypes(fields)
//                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
//                    .build();
//            milvusClient.createCollection(createCollectionParam);
//
//            // 创建索引
//            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
//                    .withCollectionName(collectionName)
//                    .withFieldName("embedding")
//                    .withIndexType(IndexType.AUTOINDEX)
//                    .withMetricType(MetricType.COSINE)
//                    .build();
//            milvusClient.createIndex(indexParam);
//
//            // 加载集合
//            milvusClient.loadCollection(
//                    LoadCollectionParam.newBuilder()
//                            .withCollectionName(collectionName)
//                            .build());
//
//            log.info("Collection {} created successfully", collectionName);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create collection", e);
//        }
//    }

    /**
     * 存储文档
     */
    @Override
    public void storeDocuments(List<Document> documents) {
        if (documents.isEmpty()) {
            return;
        }
        ragDocumentVectorStore.add(documents);
    }

    /**
     * 搜索文档
     */
    @Override
    public List<Document> search(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.7)
                .build();
        return ragDocumentVectorStore.similaritySearch(searchRequest);
    }

    /**
     * 批量搜索
     */
    @Override
    public Map<String, List<Document>> batchSearch(List<String> queries, int topK) {
        Map<String, List<Document>> results = new HashMap<>();
        for (String query : queries) {
            results.put(query, search(query, topK));
        }
        return results;
    }

    /**
     * 分页搜索
     */
    @Override
    public Page<Document> pageSearch(String query, int page, int size) {
        Page<Document> resultPage = new Page<>();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(page + size) // 获取足够的数据（可以基于滚动分页）
                .similarityThreshold(0.3)
                .build();

        List<Document> allResults = ragDocumentVectorStore.similaritySearch(searchRequest);

        resultPage.setRecords(allResults);
        resultPage.setTotal(allResults.size());
        return resultPage;
    }

    /**
     * 根据hash搜索文档
     */
    @Override
    public List<Document> searchByHash(String query, String hashValue) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(1) //默认4
                .filterExpression("hashValue == '" + hashValue + "'")
                .build();
        return ragDocumentVectorStore.similaritySearch(searchRequest);
    }

    /**
     * 删除文档
     */
    @Override
    public void deleteDocument(String docId) {
        ragDocumentVectorStore.delete(Collections.singletonList(docId));
    }

    /**
     * 清空集合
     */
    @Override
    public void clearCollection() {
        List<String> allDocIds = getAllDocumentIds();
        if (CollUtil.isNotEmpty(allDocIds)) {
            ragDocumentVectorStore.delete(allDocIds);
        }
    }

    /**
     * 获取所有文档ID
     */
    public List<String> getAllDocumentIds() {
        // 这里需要实现获取所有文档ID的逻辑
        return Collections.emptyList();
    }

}
