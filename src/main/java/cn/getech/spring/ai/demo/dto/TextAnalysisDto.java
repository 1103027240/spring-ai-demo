package cn.getech.spring.ai.demo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author 11030
 */
@Data
@Builder
public class TextAnalysisDto {

    /** 语言类型（chinese/english/unknown） */
    private String language;

    /** 文本类型（markdown/html/json/code/plain） */
    private String type;

    /** 文本总长度（字符数） */
    private int length;

    /** 文本行数 */
    private int lineCount;

}
