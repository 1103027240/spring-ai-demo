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

    private String text;

    private String type;

}
