package cn.example.ai.demo.service;

import cn.example.ai.demo.entity.Order;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
public interface OrderService {

    List<Map<String, Object>> queryOrders(Map<String, Object> queryParams);

    Order getByOrderNumber(String orderNumber);

}
