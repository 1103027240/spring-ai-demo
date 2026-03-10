package cn.getech.mcp.server.demo.service.impl;

import cn.getech.mcp.server.demo.dto.ProductInfoDto;
import cn.getech.mcp.server.demo.dto.StockInfoDto;
import cn.getech.mcp.server.demo.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * @author 11030
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Tool(description = "查询商品详细信息")
    @Override
    public ProductInfoDto getProductDetail(@ToolParam(description = "商品名称") String name) {
        log.info("检索商品名称: {}", name);
        return ProductInfoDto.builder()
                .skuId("123456")
                .name("华为手机")
                .price(new BigDecimal(5999))
                .build();
    }

    @Tool(description = "查询库存信息")
    @Override
    public StockInfoDto getStockInfo(@ToolParam(description = "商品ID") String skuId) {
        log.info("检索库存ID: {}", skuId);
        return StockInfoDto.builder()
                .skuId(skuId)
                .warehouseCode("海外仓")
                .quantity(new BigDecimal(100))
                .status("available")
                .build();
    }

}
