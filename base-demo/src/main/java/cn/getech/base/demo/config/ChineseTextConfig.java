package cn.getech.base.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 中文分割器配置类
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.text-splitting.chinese")
public class ChineseTextConfig {

    /** 中文分割 */
    private String pattern;

    /** 中文分割 - 块大小（中文字符数） */
    private int chunkSize;

    /** 中文分割 - 块重叠量 */
    private int chunkOverlap;

}
