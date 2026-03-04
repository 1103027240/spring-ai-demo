package cn.getech.spring.ai.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 代码分割器配置类
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.text-splitting.code")
public class CodeTextConfig {

    /** 代码分割 */
    private String pattern;

    /** 代码分割 - 块大小（代码字符数） */
    private int chunkSize;

    /** 代码分割 - 块重叠量 */
    private int chunkOverlap;

}
