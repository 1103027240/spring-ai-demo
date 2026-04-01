package cn.example.ai.demo.config.text;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Token分割器配置类
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.text-splitting.token")
public class TokenTextConfig {

    /** Token 分割 - 块大小（token 数） */
    private int chunkSize;

    /** Token 分割 - 最小块大小 */
    private int minChunkSize;

    /** Token 分割 - 最小块长度 */
    private int minChunkLength;

    /** Token 分割 - 最大块数 */
    private int maxNumChunks;

    /** Token 分割 - 是否保留分隔符 */
    private boolean keepSeparator;

}
