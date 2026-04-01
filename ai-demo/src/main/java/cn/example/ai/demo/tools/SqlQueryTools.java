package cn.example.ai.demo.tools;

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
import static cn.example.ai.demo.constant.PatternConstant.SQL_INJECTION_PATTERN;

@Slf4j
@Component
public class SqlQueryTools {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Tool(name = "validateSql", description = "验证SQL语句安全性")
    public Map<String, Object> validateSql(@ToolParam(name = "sql", description = "SQL语句") String sql) {
        log.info("验证SQL语句安全性开始执行: {}", sql);

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
            log.error("验证SQL语句安全性执行失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @Tool(name = "executeSql", description = "执行SQL查询语句并返回结果")
    public Map<String, Object> executeSql(@ToolParam(name = "sql", description = "SQL语句") String sql) {
        log.info("执行SQL查询语句开始执行: {}", sql);

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
            log.error("执行SQL查询语句执行失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @Tool(name = "getSchema", description = "获取数据库表结构和字段信息")
    public Map<String, Object> getSchema(@ToolParam(name = "sql", description = "表名") String tableName) {
        log.info("获取数据库结构开始执行: {}", tableName);

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
            log.error("获取数据库结构执行失败", e);
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
                    log.warn("检测到禁止SQL操作: {}", keyword);
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
