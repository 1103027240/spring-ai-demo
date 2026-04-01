package cn.example.ai.demo.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.service.IVecMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class MilvusClientConfig {

    @Value("${milvus.client.uri:http://localhost:19530}")
    private String uri;

    @Value("${milvus.client.token:}")
    private String token;

    @Value("${milvus.client.database-name:default}")
    private String databaseName;

    @Value("${milvus.client.connect-timeout-ms:10000}")
    private Long connectTimeoutMs;

    @Value("${milvus.client.rpc-timeout-ms:30000}")
    private Long rpcTimeoutMs;

    @Bean
    public MilvusClientV2 milvusClientV2() {
        try {
            log.info("开始创建 MilvusClientV2，URI: {}", uri);

            // 构建连接配置
            ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                    .uri(uri)
                    .dbName(databaseName)
                    .connectTimeoutMs(connectTimeoutMs);

            // 如果有 token，添加 token
            if (StringUtils.hasText(token)) {
                builder.token(token);
            }

            ConnectConfig connectConfig = builder.build();

            // 创建客户端
            MilvusClientV2 client = new MilvusClientV2(connectConfig);

            // 配置 RPC 超时
            if (rpcTimeoutMs > 0) {
                client.withTimeout(rpcTimeoutMs, TimeUnit.MILLISECONDS);
            }

            // 验证连接
            validateConnection(client);

            return client;
        } catch (Exception e) {
            log.error("创建 MilvusClientV2 失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法连接到 Milvus 服务: " + e.getMessage(), e);
        }
    }

    /**
     * 验证连接
     */
    public void validateConnection(MilvusClientV2 client) {
        try {
            if (!client.clientIsReady()) {
                throw new IllegalStateException("Milvus 客户端未就绪");
            }

            // 获取服务器版本
            String serverVersion = client.getServerVersion();
            log.info("成功连接到 Milvus 服务器，版本: {}", serverVersion);
        } catch (Exception e) {
            log.error("Milvus 连接验证失败: {}", e.getMessage());
            throw new RuntimeException("Milvus 连接验证失败: " + e.getMessage(), e);
        }
    }

    @Bean
    public IVecMService vecMService(MilvusClientV2 milvusClientV2) {
        return () -> milvusClientV2;
    }

}