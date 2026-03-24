package cn.example.mcp.server.demo.service;

import cn.example.mcp.server.demo.dto.ProductInfoDto;
import cn.example.mcp.server.demo.dto.StockInfoDto;

/**
 * @author 11030
 */
public interface ProductService {

    ProductInfoDto getProductDetail(String name);

    StockInfoDto getStockInfo(String skuId);

}
