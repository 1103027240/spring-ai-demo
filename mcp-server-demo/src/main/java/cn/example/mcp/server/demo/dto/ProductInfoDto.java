package cn.example.mcp.server.demo.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * @author 11030
 */
@Data
@Builder
public class ProductInfoDto {

    private String skuId;

    private String name;

    private BigDecimal price;

}
