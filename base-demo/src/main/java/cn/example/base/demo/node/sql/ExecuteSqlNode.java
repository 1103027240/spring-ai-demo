package cn.example.base.demo.node.sql;

import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import cn.example.base.demo.tools.QueryTools;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.QueryTypeEnum.SALES_ANALYSIS;
import static cn.example.base.demo.enums.QueryTypeEnum.TOP_PRODUCTS;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;

/**
 * 4、SQL执行节点
 */
@Slf4j
@Component
public class ExecuteSqlNode implements NodeAction {

    @Autowired
    private QueryTools queryTools;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】SQL执行节点开始执行");

        String queryType = "";
        String naturalLanguageQuery = "";
        String generatedSql = "";
        String dataJson = "";
        String dataSummary = "";
        Integer rowCount = null;
        Long executionTime = null;

        try {
            // 校验请求参数
            generatedSql = state.value(GENERATED_SQL, String.class).orElse("");
            if (StrUtil.isBlank(generatedSql)) {
                return Map.of(
                        ERROR, "【SQL执行节点】SQL语句为空",
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            // 调用QueryTools.executeSql
            Map<String, Object> executeSqlResult = queryTools.executeSql(generatedSql);
            boolean success = (boolean) executeSqlResult.getOrDefault(SUCCESS, false);
            if (!success) {
                return Map.of(
                        EXECUTE_SQL_RESULT, executeSqlResult,
                        ERROR, "【SQL执行节点】SQL执行失败: " + executeSqlResult.get(ERROR),
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            // 用于数据分析
            naturalLanguageQuery = state.value(NATURAL_LANGUAGE_QUERY, String.class).orElse("");

            Map<String, Object> nlToSqlResult = state.value(NL_TO_SQL_RESULT, Map.class).orElse(new HashMap<>());
            queryType = (String) nlToSqlResult.getOrDefault(QUERY_TYPE, "");

            rowCount = (int) executeSqlResult.getOrDefault(ROW_COUNT, 0);
            executionTime = (long) executeSqlResult.getOrDefault(EXECUTION_TIME, 0L);

            List<Map<String, Object>> data = (List<Map<String, Object>>) executeSqlResult.getOrDefault(DATA, new ArrayList<>());
            dataJson = generateDataJson(data);
            dataSummary = generateDataSummary(data, queryType);

            Map<String, Object> result = new HashMap<>();
            result.put(EXECUTE_SQL_RESULT, executeSqlResult);
            result.put(QUERY_TYPE, queryType);
            result.put(NATURAL_LANGUAGE_QUERY, naturalLanguageQuery);
            result.put(GENERATED_SQL, generatedSql);
            result.put(ROW_COUNT, rowCount);
            result.put(EXECUTION_TIME, executionTime);
            result.put(DATA_JSON, dataJson);
            result.put(DATA_SUMMARY, dataSummary);
            result.put(WORKFLOW_STATUS, QueryWorkflowStatusEnum.PROCESSING.getId());
            result.put(CURRENT_NODE, EXECUTE_SQL_NODE.getId());
            result.put(NEXT_NODE, ANALYSIS_NODE.getId());
            return result;
        } catch (Exception e) {
            log.error("【数据查询智能体】SQL执行节点执行失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put(QUERY_TYPE, queryType);
            result.put(NATURAL_LANGUAGE_QUERY, naturalLanguageQuery);
            result.put(GENERATED_SQL, generatedSql);
            result.put(ROW_COUNT, rowCount);
            result.put(EXECUTION_TIME, executionTime);
            result.put(DATA_JSON, dataJson);
            result.put(DATA_SUMMARY, dataSummary);
            result.put(ERROR, e.getMessage());
            result.put(WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId());
            result.put(NEXT_NODE, ERROR_HANDLE_NODE.getId());
            return result;
        }
    }

    private String generateDataJson(List<Map<String, Object>> data) {
        try {
            if (CollUtil.isEmpty(data)) {
                return "[]";
            }

            // 限制数据量，避免instruction模板过长
            List<Map<String, Object>> limitedData = data.subList(0, Math.min(data.size(), 20));
            return objectMapper.writeValueAsString(limitedData);
        } catch (Exception e) {
            log.error("【数据查询智能体】转换数据为JSON失败", e);
            return "[]";
        }
    }

    private String generateDataSummary(List<Map<String, Object>> data, String queryType) {
        if (CollUtil.isEmpty(data)) {
            return "查询结果为空";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("共【").append(data.size()).append("】条记录\n");

        // 销售分析数据摘要
        if (SALES_ANALYSIS.getId().equals(queryType)) {
            double totalSales = 0;
            for (Map<String, Object> row : data) {
                Object sales = row.get("total_sales");
                if (sales instanceof Number) {
                    totalSales += ((Number) sales).doubleValue();
                }
            }
            summary.append("销售总额: ").append(String.format("%.2f", totalSales)).append("\n");
        }

        // 热门商品数据摘要
        else if (TOP_PRODUCTS.getId().equals(queryType)) {
            summary.append("热门商品前三:\n");
            for (int i = 0; i < Math.min(3, data.size()); i++) {
                Map<String, Object> product = data.get(i);
                summary.append(i + 1).append(". ")
                        .append(product.get("product_name"))
                        .append(" - 销售额: ").append(product.get("total_sales"))
                        .append("\n");
            }
        }

        return summary.toString();
    }

}
