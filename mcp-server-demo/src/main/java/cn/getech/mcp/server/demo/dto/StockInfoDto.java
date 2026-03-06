package cn.getech.mcp.server.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 11030
 */
@Data
@Builder
public class StockInfoDto {

    private String skuId;

    private String warehouseCode;

    private BigDecimal quantity;

    private String status;

}
