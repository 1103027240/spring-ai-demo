package cn.getech.spring.ai.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 字符分割器配置类
 * @author 11030
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.text-splitting.character")
public class CharacterTextConfig {

    /** 字符分割 - 块大小（字符数） */
    private int chunkSize;

    /** 字符分割 - 块重叠量 */
    private int chunkOverlap;

    /** 是否尽量保持单词完整 **/
    private boolean characterPreserveWords;

}
