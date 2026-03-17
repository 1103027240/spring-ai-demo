package cn.getech.base.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Builder
@Data
@Schema(description = "排序信息")
public class SortInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String primaryField;

    private String primaryDirection;

    private String secondField;

    private String secondDirection;

}
