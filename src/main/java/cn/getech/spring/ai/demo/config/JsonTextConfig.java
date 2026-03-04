package cn.getech.spring.ai.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JSON分割器配置类
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.text-splitting.json")
public class JsonTextConfig {

    /** JSON 分割 */
    private String pattern;

}
