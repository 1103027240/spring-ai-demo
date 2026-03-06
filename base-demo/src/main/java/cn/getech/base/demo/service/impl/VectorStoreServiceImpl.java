package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.VectorStoreService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.milvus.client.MilvusServiceClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
@Slf4j
@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    @Autowired
    private MilvusServiceClient milvusClient;

    @Resource(name = "ragVectorStore")
    private VectorStore vectorStore;

    // 操作是milvus数据库中long_term_chat_memory集合
//    @Value("${spring.ai.vectorstore.milvus.collection-name}")
//    private String collectionName;
//
//    @Value("${spring.ai.vectorstore.milvus.embedding-dimension}")
//    private int embeddingDimension;

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
        vectorStore.add(documents);
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
        return vectorStore.similaritySearch(searchRequest);
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
                .topK(100) // 获取足够多的结果用于分页
                .similarityThreshold(0.3)
                .build();

        List<Document> allResults = vectorStore.similaritySearch(searchRequest);
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
                .filterExpression("hashValue == '" + hashValue + "'")
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    /**
     * 删除文档
     */
    @Override
    public void deleteDocument(String docId) {
        vectorStore.delete(Collections.singletonList(docId));
    }

    /**
     * 清空集合
     */
    @Override
    public void clearCollection() {
        List<String> allDocIds = getAllDocumentIds();
        if (CollUtil.isNotEmpty(allDocIds)) {
            vectorStore.delete(allDocIds);
        }
    }

    /**
     * 获取所有文档ID
     */
    public List<String> getAllDocumentIds() {
        // 这里需要实现获取所有文档ID的逻辑
        // 由于Spring AI的限制，可能需要直接使用Milvus SDK
        return Collections.emptyList();
    }

}
