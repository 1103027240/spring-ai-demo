package cn.example.base.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Markdown分割器配置类
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.text-splitting.markdown")
public class MarkdownTextConfig {

    /** Markdown 分割 */
    private String pattern;

    /** Markdown 分割 - 块大小（字符数） */
    private int chunkSize;

    /** Markdown 分割 - 块重叠量 */
    private int chunkOverlap;

}
