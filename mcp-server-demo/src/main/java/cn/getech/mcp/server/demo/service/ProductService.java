package cn.getech.mcp.server.demo.service;

import cn.getech.mcp.server.demo.dto.ProductInfoDto;
import cn.getech.mcp.server.demo.dto.StockInfoDto;

/**
 * @author 11030
 */
public interface ProductService {

    ProductInfoDto getProductDetail(String name);

    StockInfoDto getStockInfo(String skuId);

}
