package cn.example.base.demo.param.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文本分段类：表示文本中的一个片段及其类型
 * @author 11030
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextSegmentDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文本片段
     */
    private String text;

    /**
     * 文本片段分割算法
     */
    private String algorithm;

}
