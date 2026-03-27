package cn.example.base.demo.node.sql;

import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.QueryTypeEnum.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;

/**
 * 6、查询结果生成节点
 */
@Slf4j
@Component
public class SqlResultGenerateNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】查询结果生成节点开始执行");

        try {
            Map<String, Object> nlToSqlResult = state.value(NL_TO_SQL_RESULT, Map.class).orElse(new HashMap<>());
            Map<String, Object> executeSqlResult = state.value(EXECUTE_SQL_RESULT, Map.class).orElse(new HashMap<>());
            Map<String, Object> analysisResult = state.value(ANALYSIS_RESULT, Map.class).orElse(new HashMap<>());
            String naturalLanguageQuery = state.value(NATURAL_LANGUAGE_QUERY, String.class).orElse("");
            String generatedSql = state.value(GENERATED_SQL, String.class).orElse("");
            String workflowId = state.value(WORKFLOW_ID, String.class).orElse("");

            // 构建最终结果
            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put(WORKFLOW_ID, workflowId);
            finalResult.put(NATURAL_LANGUAGE_QUERY, naturalLanguageQuery);
            finalResult.put(GENERATED_SQL, generatedSql);
            finalResult.put(QUERY_TYPE, nlToSqlResult.get(QUERY_TYPE));
            finalResult.put(WORKFLOW_STATUS, QueryWorkflowStatusEnum.COMPLETED.getId());
            finalResult.put(SUCCESS, true);

            // sql执行结果
            if (executeSqlResult.containsKey(DATA)) {
                finalResult.put(DATA, executeSqlResult.get(DATA));
                finalResult.put("rowCount", executeSqlResult.get("rowCount"));
                finalResult.put("executionTime", executeSqlResult.get("executionTime"));
            }

            // 数据分析结果
            if (analysisResult.containsKey("analysis")) {
                finalResult.put("aiAnalysis", analysisResult);
            } else {
                if (executeSqlResult.containsKey(DATA)) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) executeSqlResult.get(DATA);
                    Map<String, Object> manualAnalysis = performSimpleAnalysis(data, (String) nlToSqlResult.get(QUERY_TYPE));
                    finalResult.put("manualAnalysis", manualAnalysis);
                }
            }

            return Map.of(
                    FINAL_RESULT, finalResult,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.COMPLETED.getId(),
                    CURRENT_NODE, QUERY_RESULT_GENERATE.getId(),
                    NEXT_NODE, StateGraph.END,
                    SUCCESS, true);
        } catch (Exception e) {
            log.error("【数据查询智能体】查询结果生成节点执行失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getId());
        }
    }

    private Map<String, Object> performSimpleAnalysis(List<Map<String, Object>> data, String queryType) {
        Map<String, Object> analysis = new HashMap<>();
        if (CollUtil.isEmpty(data)) {
            analysis.put(MESSAGE, "sql返回结果为空");
            return analysis;
        }

        try {
            analysis.put("rowCount", data.size());
            analysis.put(QUERY_TYPE, queryType);

            // 销售分析
            if (SALES_ANALYSIS.getId().equals(queryType)) {
                double totalSales = 0;
                double maxAmount = 0;
                double minAmount = Double.MAX_VALUE;
                int dayCount = 0;

                for (Map<String, Object> row : data) {
                    Object amount = row.get("total_sales");
                    if (amount instanceof Number) {
                        double sales = ((Number) amount).doubleValue();
                        totalSales += sales;
                        maxAmount = Math.max(maxAmount, sales);
                        minAmount = Math.min(minAmount, sales);
                        dayCount++;
                    }
                }

                analysis.put("totalSales", totalSales);
                analysis.put("maxDailySales", maxAmount);
                analysis.put("minDailySales", minAmount);
                analysis.put("avgDailySales", dayCount > 0 ? totalSales / dayCount : 0);
                analysis.put("dayCount", dayCount);
            }

            // 商品分析
            else if (TOP_PRODUCTS.getId().equals(queryType)) {
                List<Map<String, Object>> topProducts = new ArrayList<>();
                for (int i = 0; i < Math.min(5, data.size()); i++) {
                    topProducts.add(data.get(i));
                }
                analysis.put("topProducts", topProducts);
            }

            // 客户分析
            else if (CUSTOMER_ANALYSIS.getId().equals(queryType)) {
                double totalSpent = 0;
                int totalOrders = 0;
                int customerCount = 0;

                for (Map<String, Object> row : data) {
                    Object spent = row.get("total_spent");
                    Object orders = row.get("order_count");

                    if (spent instanceof Number) {
                        totalSpent += ((Number) spent).doubleValue();
                        customerCount++;
                    }
                    if (orders instanceof Number) {
                        totalOrders += ((Number) orders).intValue();
                    }
                }

                analysis.put("totalSpentByCustomers", totalSpent);
                analysis.put("totalOrdersByCustomers", totalOrders);
                analysis.put("customerCount", customerCount);
                analysis.put("avgSpentPerCustomer", customerCount > 0 ? totalSpent / customerCount : 0);
            }

            analysis.put(SUCCESS, true);
            analysis.put(MESSAGE, "简单数据分析完成");
        } catch (Exception e) {
            log.error("【数据查询智能体】查询结果生成节点执行失败", e);
            analysis.put(SUCCESS, false);
            analysis.put(MESSAGE, e.getMessage());
        }

        return analysis;
    }

}
