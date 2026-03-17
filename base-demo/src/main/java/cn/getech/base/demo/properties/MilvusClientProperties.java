package cn.getech.base.demo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "milvus.client")
public class MilvusClientProperties {

    // 连接方式1: 分别指定 host 和 port
    private String host = "localhost";
    private Integer port = 19530;

    // 连接方式2: 使用 URI
    private String uri;

    // 连接池配置
    private Integer maxConnections = 20;
    private Long connectionTimeoutMs = 30000L;
    private Long rpcTimeoutMs = 60000L;
    private Long rpcDeadlineMs = 30000L;
    private Long keepAliveTimeMs = 30000L;

    // 数据库配置
    private String databaseName = "default";

    // 认证配置
    private String username;
    private String password;

    // TLS/SSL 配置
    private Boolean enableTls = false;
    private String clientPfxPath;
    private String clientPfxPassword;
    private String serverCaPath;

    // 重试配置
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class RetryConfig {
        private Integer maxAttempts = 3;
        private Long baseDelayMs = 1000L;
        private Long maxDelayMs = 10000L;
        private Double jitter = 0.5;
    }

}
