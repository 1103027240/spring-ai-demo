package cn.example.base.demo.param.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @author 11030
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReviewDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String documentContent;

}
