package cn.getech.spring.ai.demo.dto;

import lombok.*;

/**
 * 文本分段类：表示文本中的一个片段及其类型
 * @author 11030
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextSegmentDto {

    /**
     * 文本片段
     */
    private String text;

    /**
     * 文本片段分割算法
     */
    private String algorithm;

}
