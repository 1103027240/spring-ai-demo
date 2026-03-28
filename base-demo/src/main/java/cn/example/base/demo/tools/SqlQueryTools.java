package cn.example.base.demo.tools;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cn.example.base.demo.constant.PatternConstant.SQL_INJECTION_PATTERN;
import static cn.example.base.demo.enums.SqlQueryTypeEnum.*;

@Slf4j
@Component
public class SqlQueryTools {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Tool(name = "nlToSql", description = "将自然语言查询转SQL语句")
    public Map<String, Object> nlToSql(@ToolParam(name = "naturalLanguageQuery", description = "自然语言查询") String naturalLanguageQuery) {
        Map<String, Object> result = new HashMap<>();
        String generatedSql = "";
        String queryType = "";

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
            log.error("自然语言转SQL失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @Tool(name = "validateSql", description = "验证SQL语句的安全性")
    public Map<String, Object> validateSql(String sql) {
        try {
            Map<String, Object> result = new HashMap<>();
            if (StrUtil.isBlank(sql) || StrUtil.isBlank(sql.trim())) {
                result.put("success", false);
                result.put("riskLevel", "HIGH");
                result.put("error", "SQL语句为空");
                return result;
            }

            String upperSql = sql.trim().toUpperCase();

            // 1. 必须以SELECT开头
            if (!upperSql.startsWith("SELECT")) {
                result.put("success", false);
                result.put("riskLevel", "HIGH");
                result.put("error", "只允许SELECT查询");
                return result;
            }

            // 2. 必须包含LIMIT
            if (!upperSql.contains("LIMIT")) {
                result.put("success", false);
                result.put("riskLevel", "MEDIUM");
                result.put("error", "必须包含LIMIT限制");
                return result;
            }

            // 3. 检查SQL注入
            if (SQL_INJECTION_PATTERN.matcher(sql).find()) {
                result.put("success", false);
                result.put("riskLevel", "CRITICAL");
                result.put("error", "检测到SQL注入特征");
                return result;
            }

            result.put("success", true);
            result.put("riskLevel", "LOW");
            result.put("error", "SQL验证通过");
            return result;
        } catch (Exception e) {
            log.error("验证SQL语句失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @Tool(name = "executeSql", description = "执行SQL查询语句并返回结果")
    public Map<String, Object> executeSql(String sql) {
        try {
            // 验证SQL安全性
            if (!validateSqlSafety(sql)) {
                return Map.of("success", false, "error", "SQL语句安全性验证失败");
            }

            // 执行查询
            long startTime = System.currentTimeMillis();
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            // 脱敏处理
            List<Map<String, Object>> maskedData = maskSensitiveData(data);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", maskedData);
            result.put("rowCount", data.size());
            result.put("executionTime", executionTime);
            result.put("sql", sql);
            return result;
        } catch (Exception e) {
            log.error("执行SQL查询失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @Tool(name = "getSchema", description = "获取数据库表结构和字段信息")
    public Map<String, Object> getSchema(String tableName) {
        try {
            String sql = """
                SELECT 
                    TABLE_NAME as tableName,
                    COLUMN_NAME as columnName,
                    DATA_TYPE as dataType,
                    IS_NULLABLE as nullable,
                    COLUMN_DEFAULT as defaultValue,
                    COLUMN_COMMENT as comment
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                """;

            if (StrUtil.isNotBlank(tableName) && StrUtil.isNotBlank(tableName.trim())) {
                sql += " AND TABLE_NAME = '" + tableName + "'";
            }

            sql += " ORDER BY TABLE_NAME, ORDINAL_POSITION";

            List<Map<String, Object>> schema = jdbcTemplate.queryForList(sql);

            // 按表名分组
            Map<String, List<Map<String, Object>>> groupedSchema = new HashMap<>();
            for (Map<String, Object> column : schema) {
                String table = (String) column.get("tableName");
                groupedSchema.computeIfAbsent(table, k -> new ArrayList<>()).add(column);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("schema", groupedSchema);
            result.put("tableCount", groupedSchema.size());
            result.put("columnCount", schema.size());
            return result;
        } catch (Exception e) {
            log.error("获取数据库结构失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private boolean validateSqlSafety(String sql) {
        try {
            String upperSql = sql.toUpperCase();

            // 禁止操作（CREATE TABLE、UPDATE TABLE因为有创建和修改时间）
            String[] forbiddenKeywords = {
                    "INSERT", "UPDATE TABLE", "DELETE", "DROP", "TRUNCATE", "ALTER", "CREATE TABLE",
                    "GRANT", "REVOKE", "EXEC", "WAITFOR", "SHUTDOWN"
            };

            for (String keyword : forbiddenKeywords) {
                if (upperSql.contains(keyword)) {
                    log.warn("检测到禁止的SQL操作: {}", keyword);
                    return false;
                }
            }

            // 必须包含LIMIT
            if (!upperSql.contains("LIMIT")) {
                log.warn("SQL缺少LIMIT限制");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("SQL安全性验证异常", e);
            return false;
        }
    }

    private List<Map<String, Object>> maskSensitiveData(List<Map<String, Object>> data) {
        if (CollUtil.isEmpty(data)) {
            return data;
        }

        List<Map<String, Object>> maskedData = new ArrayList<>();

        for (Map<String, Object> row : data) {
            Map<String, Object> maskedRow = new HashMap<>(row);

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey().toLowerCase();
                Object value = entry.getValue();

                // 对敏感信息进行脱敏
                if (value instanceof String) {
                    String strValue = (String) value;

                    // 手机号脱敏
                    if (key.contains("phone") || key.contains("tel") || key.contains("mobile")) {
                        if (strValue.length() >= 7) {
                            maskedRow.put(entry.getKey(), strValue.substring(0, 3) + "****" + strValue.substring(strValue.length() - 4));
                        }
                    }

                    // 邮箱脱敏
                    else if (key.contains("email")) {
                        int atIndex = strValue.indexOf('@');
                        if (atIndex > 1) {
                            maskedRow.put(entry.getKey(), strValue.substring(0, 1) + "***" + strValue.substring(atIndex - 1));
                        }
                    }

                    // 身份证脱敏
                    else if (key.contains("idno") || key.contains("idcard") || key.contains("身份证")) {
                        if (strValue.length() >= 8) {
                            maskedRow.put(entry.getKey(), strValue.substring(0, 3) + "***********" + strValue.substring(strValue.length() - 4));
                        }
                    }

                    // 地址脱敏
                    else if (key.contains("address") || key.contains("地址")) {
                        if (strValue.length() > 10) {
                            maskedRow.put(entry.getKey(), strValue.substring(0, 6) + "****" + strValue.substring(strValue.length() - 4));
                        }
                    }
                }
            }

            maskedData.add(maskedRow);
        }

        return maskedData;
    }

}
