package cn.getech.base.demo.config;

import cn.getech.base.demo.memory.HierarchicalChatMemory;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.redis.BaseRedisChatMemoryRepository;
import com.alibaba.cloud.ai.memory.redis.LettuceRedisChatMemoryRepository;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 大语言模型配置
 * @author 11030
 */
@Configuration
public class SaaLLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.qwen.model:qwen-plus}")
    private String qwenModel;

    @Value("${spring.ai.dashscope.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    // Redis配置
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.data.redis.timeout:18000}")
    private int redisTimeout;

    @Value("${spring.ai.memory.short-term.max-messages:10}")
    private int maxMessages;

    @Value("${spring.ai.memory.redis.key-prefix:spring_ai_long_chat_memory}")
    private String memoryKeyPrefix;

    @Resource(name = "ragVectorStore")
    private VectorStore vectorStore;

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public ChatModel qwenChatModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model(qwenModel)
                        .temperature(0.7d)
                        .topP(0.9d)
                        .maxToken(2048)
                        .build())
                .build();
    }

    @Bean
    public ChatModel deepseekChatModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model(deepseekModel)
                        .temperature(0.7d)
                        .topP(0.9d)
                        .maxToken(2048)
                        .build())
                .build();
    }

    /**
     * 使用Lettuce实现Redis短期记忆存储
     */
    @Bean
    @ConditionalOnProperty(name = "spring.ai.memory.implementation", havingValue = "lettuce", matchIfMissing = true)
    public BaseRedisChatMemoryRepository redisChatMemoryRepository() {
        return createLettuceRepository(memoryKeyPrefix);
    }

    /**
     * 使用Redisson实现Redis短期记忆存储
     */
//    @Bean
//    @ConditionalOnProperty(name = "spring.ai.memory.implementation", havingValue = "redisson")
//    public BaseRedisChatMemoryRepository redissonRedisChatMemoryRepository() {
//        return createRedissonRepository(memoryKeyPrefix);
//    }

    /**
     * 创建 Lettuce Redis 存储的通用方法
     */
    private BaseRedisChatMemoryRepository createLettuceRepository(String keyPrefix) {
        LettuceRedisChatMemoryRepository.RedisBuilder builder = LettuceRedisChatMemoryRepository.builder()
                .host(redisHost)
                .port(redisPort)
                .timeout(redisTimeout)
                .keyPrefix(keyPrefix);

        // 只有当密码不为空时才设置密码
        if (StrUtil.isNotBlank(redisPassword)) {
            builder.password(redisPassword);
        }

        return builder.build();
    }

    /**
     * 创建 Redisson Redis 存储的通用方法
     */
//    private BaseRedisChatMemoryRepository createRedissonRepository(String keyPrefix) {
//        RedissonRedisChatMemoryRepository.RedissonBuilder builder = RedissonRedisChatMemoryRepository.builder()
//                .host(redisHost)
//                .port(redisPort)
//                .timeout(redisTimeout)
//                .keyPrefix(keyPrefix);
//
//        // 只有当密码不为空时才设置密码
//        if (StrUtil.isNotBlank(redisPassword)) {
//            builder.password(redisPassword);
//        }
//
//        return builder.build();
//    }

    /**
     * 短期记忆实现，基于消息窗口的记忆机制，保留最新的N条对话记录
     */
    @Bean
    public ChatMemory shortTermMemory(BaseRedisChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .build();
    }

    /**
     * 分层记忆实现，结合短期记忆（Redis）和长期记忆（Milvus向量库）
     */
    @Bean
    public ChatMemory hierarchicalChatMemory(
            ChatMemory shortTermMemory,
            @Qualifier("longTermMemoryVectorStore") VectorStore vectorStore) {
        return new HierarchicalChatMemory(shortTermMemory, vectorStore);
    }

    /**
     * 常用qwenChatClient
     */
    @Bean
    public ChatClient qwenChatClient(ChatModel qwenChatModel, ChatMemory hierarchicalChatMemory) {
        return ChatClient.builder(qwenChatModel).build();
    }

    /**
     * 带分层记忆的qwenChatClient
     */
    @Bean
    public ChatClient memoryQwenChatClient(ChatModel qwenChatModel, ChatMemory hierarchicalChatMemory) {
        return ChatClient.builder(qwenChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(hierarchicalChatMemory)
                        .build())
                .build();
    }

    /**
     * 带RAG的qwenChatClient：先通过知识库搜索，会自动组装上下文，然后调用AI大模型查询
     */
    @Bean
    public ChatClient ragQwenChatClient(ChatModel qwenChatModel, ChatMemory hierarchicalChatMemory) {
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).build())
                .build();
        return ChatClient.builder(qwenChatModel)
                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }

    @Bean
    public ChatClient deepseekChatClient(ChatModel deepseekChatModel, ChatMemory hierarchicalChatMemory) {
        return ChatClient.builder(deepseekChatModel).build();
    }

}



