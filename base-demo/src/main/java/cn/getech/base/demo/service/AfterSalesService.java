package cn.getech.base.demo.service;

import java.util.Map;

/**
 * @author 11030
 */
public interface AfterSalesService {

    Map<String, Object> processReturnRequest(String userInput);

    Map<String, Object> processExchangeRequest(String userInput);

    Map<String, Object> processRefundRequest(String userInput);

    Map<String, Object> queryAfterSalesProgress(String serviceNumber);

}
