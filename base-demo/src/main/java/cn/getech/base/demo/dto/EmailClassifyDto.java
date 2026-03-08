package cn.getech.base.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailClassifyDto {

    /**
     * 意图：question, bug, billing, feature, complex
     */
    private String intent;

    /**
     * 紧急程度：low, medium, high, critical
     */
    private String urgency;

    /**
     * 主题
     */
    private String topic;

    /**
     * 摘要
     */
    private String summary;

}
