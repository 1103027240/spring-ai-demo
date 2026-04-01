package cn.example.ai.demo.config.text;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(value = "rag.text-splitting.recursive")
public class RecursiveCharacterTextConfig {

    /** 递归字符分割 */
    private String[] pattern;

    /** 递归字符分割 - 块大小（字符数） */
    private int chunkSize;

}
