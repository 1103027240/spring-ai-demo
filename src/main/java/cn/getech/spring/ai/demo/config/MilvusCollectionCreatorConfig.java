package cn.getech.spring.ai.demo.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.*;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 项目启动时，Milvus 数据库自动创建多集合
 * spring-ai-commons-1.1.2.jar 中，document 对应 milvus 集合必有 4 个字段 doc_id(VarChar)、content(VarChar)、embedding（FloatVector）、metadata（元数据，JSON）
 * @author 11030
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.vectorstore.milvus")
@ConditionalOnProperty(name = "spring.ai.vectorstore.milvus.auto-create-collection", havingValue = "true", matchIfMissing = true)
public class MilvusCollectionCreatorConfig {

    private static final String FIELD_DOC_ID = "doc_id";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_METADATA = "metadata";
    private static final String FIELD_EMBEDDING = "embedding";
    private static final int docIdMaxLength = 64;
    private static final int indexNlist = 128;
    private static final int contentMaxLength = 4096;

    private String uri;
    private String token;
    private String databaseName;
    private List<String> collections;
    private int embeddingDimension = 1024;
    private String indexType = "IVF_FLAT";
    private String metricType = "COSINE";

    private MilvusServiceClient milvusClient;

    /**
     * 初始化所有集合
     */
    @PostConstruct
    public void initMilvusCollections() {
        log.info("开始初始化 Milvus 集合，数据库：{}，集合列表：{}", databaseName, collections);

        try {
            initializeClient();

            if (CollUtil.isEmpty(collections)) {
                log.warn("未配置任何集合名称，跳过创建");
                return;
            }
            for (String collectionName : collections) {
                createSingleCollection(collectionName);
            }

            log.info("所有 Milvus 集合创建完成，共 {} 个", collections.size());
        } catch (Exception e) {
            throw new RuntimeException("初始化 Milvus 集合失败：" + e.getMessage(), e);
        }
    }

    /**
     * 初始化 Milvus 客户端连接
     */
    private void initializeClient() {
        log.debug("初始化 Milvus 客户端，URI：{}，数据库：{}", uri, databaseName);

        String[] credentials = parseCredentials(token);
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUri(uri)
                .withDatabaseName(databaseName)
                .withAuthorization(credentials[0], credentials[1])
                .build();
        milvusClient = new MilvusServiceClient(connectParam);

        log.debug("Milvus 客户端初始化成功");
    }

    /**
     * 创建单个集合（创建 - 索引 - 加载）
     */
    private void createSingleCollection(String collectionName) {
        validateCollectionName(collectionName);
        if (collectionExists(collectionName)) {
            log.info("集合 {} 已存在，跳过创建", collectionName);
            return;
        }

        log.info("开始创建集合：{}", collectionName);

        createCollection(collectionName);
        createIndex(collectionName);
        loadCollection(collectionName);

        log.info("集合 {} 创建成功", collectionName);
    }

    /**
     * 创建 Milvus 集合及字段定义
     */
    private void createCollection(String collectionName) {
        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .addFieldType(buildField(FIELD_DOC_ID, DataType.VarChar, docIdMaxLength, true))
                .addFieldType(buildField(FIELD_CONTENT, DataType.VarChar, contentMaxLength, false))
                .addFieldType(buildField(FIELD_METADATA, DataType.JSON, 0, false))
                .addFieldType(buildField(FIELD_EMBEDDING, DataType.FloatVector, embeddingDimension, false))
                .build();
        R<RpcStatus> createStatus = milvusClient.createCollection(createParam);
        if (createStatus.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("创建集合失败：" + createStatus.getMessage());
        }
        log.info("集合创建成功");
    }

    /**
     * 为 embedding 字段创建向量索引
     */
    private void createIndex(String collectionName) {
        log.info("开始创建向量索引，类型：{}，度量方式：{}", indexType, metricType);

        IndexType parsedIndexType = parseIndexType(indexType);
        MetricType parsedMetricType = parseMetricType(metricType);
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(FIELD_EMBEDDING)
                .withIndexType(parsedIndexType)
                .withMetricType(parsedMetricType)
                .withExtraParam("{\"nlist\":" + indexNlist + "}")
                .build();
        R<RpcStatus> indexStatus = milvusClient.createIndex(indexParam);
        if (indexStatus.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("创建向量索引失败：" + indexStatus.getMessage());
        }

        log.info("向量索引创建成功");
    }

    /**
     * 加载集合到内存
     */
    private void loadCollection(String collectionName) {
        log.info("正在加载集合到内存：{}", collectionName);

        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<RpcStatus> loadStatus = milvusClient.loadCollection(loadParam);
        if (loadStatus.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("加载集合失败：" + loadStatus.getMessage());
        }

        log.info("集合加载成功");
    }

    /**
     * 解析认证信息
     */
    private String[] parseCredentials(String token) {
        if (StrUtil.isBlank(token) || !token.contains(":")) {
            return new String[]{"root", "Milvus"};
        }
        String[] parts = token.split(":", 2);
        return parts.length == 2 ? parts : new String[]{"root", "Milvus"};
    }

    /**
     * 验证集合名称格式
     */
    private void validateCollectionName(String collectionName) {
        if (StrUtil.isBlank(collectionName) || collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("集合名称不能为空");
        }
        if (!collectionName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("集合名称格式不正确：" + collectionName);
        }
    }

    /**
     * 检查集合是否已存在
     */
    private boolean collectionExists(String collectionName) {
        HasCollectionParam hasParam = HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<Boolean> hasCollection = milvusClient.hasCollection(hasParam);
        if (hasCollection.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("检查集合状态失败：" + hasCollection.getMessage());
        }
        return Boolean.TRUE.equals(hasCollection.getData());
    }

    /**
     * 构建字段
     * @param name 字段名
     * @param dataType 数据类型
     * @param dimensionOrLength 维度或长度
     * @param isPrimaryKey 是否主键
     */
    private FieldType buildField(String name, DataType dataType, int dimensionOrLength, boolean isPrimaryKey) {
        FieldType.Builder builder = FieldType.newBuilder()
                .withName(name)
                .withDataType(dataType);

        if (dataType == DataType.VarChar) {
            builder.withMaxLength(dimensionOrLength);
        } else if (dataType == DataType.FloatVector) {
            builder.withDimension(dimensionOrLength);
        }

        if (isPrimaryKey) {
            builder.withPrimaryKey(true);
        }

        return builder.build();
    }

    /**
     * 解析索引类型枚举（失败时使用默认值）
     */
    private IndexType parseIndexType(String type) {
        try {
            return IndexType.valueOf(type);
        } catch (IllegalArgumentException e) {
            log.warn("无效的索引类型：{}，使用默认值 IVF_FLAT", type);
            return IndexType.IVF_FLAT;
        }
    }

    /**
     * 解析度量类型枚举（失败时使用默认值）
     */
    private MetricType parseMetricType(String type) {
        try {
            return MetricType.valueOf(type);
        } catch (IllegalArgumentException e) {
            log.warn("无效的度量类型：{}，使用默认值 COSINE", type);
            return MetricType.COSINE;
        }
    }

}
