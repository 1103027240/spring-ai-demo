package cn.getech.base.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 11030
 */
@Data
@Builder
public class DocumentReviewDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String documentContent;

}
