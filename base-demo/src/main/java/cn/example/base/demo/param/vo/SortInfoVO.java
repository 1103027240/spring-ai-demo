package cn.example.base.demo.param.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
@Schema(description = "排序信息")
public class SortInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String primaryField;

    private String primaryDirection;

    private String secondField;

    private String secondDirection;

}
