package cn.getech.spring.ai.demo.config;

import io.milvus.client.MilvusServiceClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 配置Milvus多集合存储VectorStore
 * @author 11030
 */
@Configuration
public class VectorStoreConfig {

    /**
     * RAG向量存储
     */
    @Bean("ragVectorStore")
    @Primary
    public VectorStore ragDocumentVectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("rag_document")
                .build();
    }

    /**
     * 长期记忆向量存储
     */
    @Bean("longTermMemoryVectorStore")
    public VectorStore longTermMemoryVectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("long_term_chat_memory")
                .build();
    }

}