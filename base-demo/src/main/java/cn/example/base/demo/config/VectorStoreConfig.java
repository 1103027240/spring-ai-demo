package cn.example.base.demo.config;

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
     * RAG测试示例向量存储
     */
    @Bean("ragDocumentVectorStore")
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
                .collectionName("long_term_memory")
                .build();
    }

    /**
     * 售后客服工作流知识库向量存储
     */
    @Bean("customerKnowledgeVectorStore")
    public VectorStore customerKnowledgeVectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("customer_knowledge")
                .build();
    }

    /**
     * 售后客服工作流会话消息向量存储
     */
    @Bean("chatMessageVectorStore")
    public VectorStore chatMessageVectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("chat_message")
                .build();
    }

}