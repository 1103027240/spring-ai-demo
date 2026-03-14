package cn.getech.base.demo.service;

import cn.getech.base.demo.entity.Order;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
public interface OrderService {

    List<Map<String, Object>> queryOrders(Map<String, Object> queryParams);

    Order getByOrderId(String orderNumber);

}
