package cn.example.base.demo.service;

import java.util.Map;

/**
 * @author 11030
 */
public interface AfterSalesService {

    Map<String, Object> processReturnRequest(String userInput, Long userId);

    Map<String, Object> processExchangeRequest(String userInput, Long userId);

    Map<String, Object> processRepairRequest(String userInput, Long userId);

    Map<String, Object> processRefundRequest(String userInput, Long userId);

    Map<String, Object> queryAfterSalesProgress(String serviceNumber);

}
