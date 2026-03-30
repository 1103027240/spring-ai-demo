package cn.example.base.demo.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static cn.example.base.demo.enums.SqlQueryTypeEnum.*;
import static cn.example.base.demo.enums.SqlQueryTypeEnum.CUSTOMER_ANALYSIS;
import static cn.example.base.demo.enums.SqlQueryTypeEnum.GENERAL_QUERY;
import static cn.example.base.demo.enums.SqlQueryTypeEnum.INVENTORY_INFO;
import static cn.example.base.demo.enums.SqlQueryTypeEnum.PAYMENT_INFO;

@Slf4j
@Component
public class NlToSqlDemoTools {

    @Tool(name = "nlToSqlDemo", description = "将自然语言查询转SQL语句")
    public Map<String, Object> nlToSqlDemo(@ToolParam(name = "naturalLanguageQuery", description = "自然语言查询") String naturalLanguageQuery) {
        Map<String, Object> result = new HashMap<>();
        String generatedSql;
        String queryType;

        // 1、最近7天销售总额/订单平均金额
        try {
            if (naturalLanguageQuery.contains("销售总额") || naturalLanguageQuery.contains("订单平均金额") ||
                    naturalLanguageQuery.contains("sales") || naturalLanguageQuery.contains("average order")) {
                queryType = SALES_ANALYSIS.getId();

                if (naturalLanguageQuery.contains("最近7天") || naturalLanguageQuery.contains("最近一周") ||
                        naturalLanguageQuery.contains("last 7 days")) {
                    generatedSql = """
                            SELECT 
                                DATE_FORMAT(create_time, '%Y-%m-%d') as order_date,
                                COUNT(*) as order_count,
                                SUM(final_amount) as total_sales,
                                AVG(final_amount) as avg_order_amount
                            FROM t_order 
                            WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                                AND status IN ('SUCCESS', 'COMPLETED')
                            GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
                            ORDER BY order_date DESC
                            LIMIT 10
                            """;
                } else {
                    generatedSql = """
                            SELECT 
                                COUNT(*) as order_count,
                                SUM(final_amount) as total_sales,
                                AVG(final_amount) as avg_order_amount
                            FROM t_order 
                            WHERE status IN ('SUCCESS', 'COMPLETED')
                            LIMIT 1
                            """;
                }
            }

            // 2、最近7天订单信息
            else if (naturalLanguageQuery.contains("最近7天") || naturalLanguageQuery.contains("最近一周") ||
                    naturalLanguageQuery.contains("last 7 days") || naturalLanguageQuery.contains("recent orders")) {
                queryType = RECENT_ORDERS.getId();

                generatedSql = """
                        SELECT 
                            order_no,
                            customer_name,
                            product_name,
                            quantity,
                            unit_price,
                            final_amount,
                            status,
                            DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') as create_time
                        FROM t_order 
                        WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                        ORDER BY create_time DESC
                        LIMIT 10
                        """;
            }

            // 3、热门商品/畅销商品
            else if (naturalLanguageQuery.contains("热门商品") || naturalLanguageQuery.contains("畅销商品") ||
                    naturalLanguageQuery.contains("best selling") || naturalLanguageQuery.contains("top products")) {
                queryType = TOP_PRODUCTS.getId();

                generatedSql = """
                        SELECT 
                            p.name as product_name,
                            p.category,
                            p.brand,
                            COUNT(o.id) as order_count,
                            SUM(o.quantity) as total_quantity,
                            SUM(o.final_amount) as total_sales
                        FROM t_order o
                        INNER JOIN t_product p ON o.product_id = p.id
                        WHERE o.status IN ('SUCCESS', 'COMPLETED')
                            AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                        GROUP BY o.product_id, p.name, p.category, p.brand
                        ORDER BY total_sales DESC
                        LIMIT 5
                        """;
            }

            // 4、客户分析/客户消费
            else if (naturalLanguageQuery.contains("客户分析") || naturalLanguageQuery.contains("客户消费") ||
                    naturalLanguageQuery.contains("customer analysis") || naturalLanguageQuery.contains("customer spending")) {
                queryType = CUSTOMER_ANALYSIS.getId();

                generatedSql = """
                        SELECT 
                            c.name as customer_name,
                            c.customer_type,
                            c.membership_level,
                            c.credit_level,
                            COUNT(o.id) as order_count,
                            SUM(o.final_amount) as total_spent,
                            MAX(DATE_FORMAT(o.create_time, '%Y-%m-%d %H:%i:%s')) as last_order_time 
                        FROM t_customer c
                        LEFT JOIN t_order o ON c.id = o.customer_id AND o.status IN ('SUCCESS', 'COMPLETED')
                        GROUP BY c.id, c.name, c.customer_type, c.membership_level, c.credit_level
                        ORDER BY total_spent DESC
                        LIMIT 10
                        """;
            }

            // 5、支付信息
            else if (naturalLanguageQuery.contains("支付信息") || naturalLanguageQuery.contains("payment") ||
                    naturalLanguageQuery.contains("transaction")) {
                queryType = PAYMENT_INFO.getId();

                generatedSql = """
                        SELECT 
                            payment_no,
                            order_no,
                            amount,
                            payment_method,
                            status,
                            DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') as create_time,
                            DATE_FORMAT(completed_time, '%Y-%m-%d %H:%i:%s') as completed_time
                        FROM t_payment
                        WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                        ORDER BY create_time DESC
                        LIMIT 10
                        """;
            }

            // 6、库存信息
            else if (naturalLanguageQuery.contains("库存") || naturalLanguageQuery.contains("inventory") ||
                    naturalLanguageQuery.contains("stock")) {
                queryType = INVENTORY_INFO.getId();

                generatedSql = """
                        SELECT 
                            product_name,
                            sku_code,
                            total_stock,
                            available_stock,
                            reserved_stock,
                            safety_stock,
                            CASE 
                                WHEN available_stock <= safety_stock THEN '需补货'
                                WHEN available_stock <= safety_stock * 2 THEN '预警'
                                ELSE '充足'
                            END as inventory_status
                        FROM t_inventory
                        ORDER BY available_stock ASC
                        LIMIT 10
                        """;
            }

            // 7、默认查询
            else {
                queryType = GENERAL_QUERY.getId();

                generatedSql = """
                        SELECT 
                            '请使用更具体的查询条件' as hint,
                            '支持查询类型: 最近7天销售额, 热门商品, 客户分析, 支付记录, 库存信息' as supported_queries
                        FROM DUAL
                        LIMIT 1
                        """;
            }

            result.put("success", true);
            result.put("originalQuery", naturalLanguageQuery);
            result.put("generatedSql", generatedSql);
            result.put("queryType", queryType);
            return result;
        } catch (Exception e) {
            log.error("将自然语言查询转SQL语句执行失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

}
