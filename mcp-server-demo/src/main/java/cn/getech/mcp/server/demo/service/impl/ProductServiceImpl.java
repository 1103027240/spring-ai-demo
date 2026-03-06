package cn.getech.mcp.server.demo.service.impl;

import cn.getech.mcp.server.demo.service.ProductService;
import cn.getech.mcp.server.demo.dto.ProductInfoDto;
import cn.getech.mcp.server.demo.dto.StockInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * @author 11030
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @McpTool(description = "查询商品详细信息")
    @Override
    public ProductInfoDto getProductDetail(
            @McpToolParam(required = false, description = "商品名称") String name) {
        log.info("检索商品名称: {}", name);
        return ProductInfoDto.builder()
                .skuId("123456")
                .name("华为手机")
                .price(new BigDecimal(5999))
                .build();
    }

    @McpTool(description = "查询库存信息")
    @Override
    public StockInfoDto getStockInfo(@McpToolParam(description = "商品ID") String skuId) {
        log.info("检索库存ID: {}", skuId);
        return StockInfoDto.builder()
                .skuId(skuId)
                .warehouseCode("海外仓")
                .quantity(new BigDecimal(100))
                .status("available")
                .build();
    }

}
