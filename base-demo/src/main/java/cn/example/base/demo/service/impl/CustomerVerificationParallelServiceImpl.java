package cn.example.base.demo.service.impl;

import cn.example.base.demo.build.MultiAgentBuild;
import cn.example.base.demo.service.CustomerVerificationParallelService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerVerificationParallelServiceImpl implements CustomerVerificationParallelService {

    @Resource(name = "customerVerificationParallelAgent")
    private ParallelAgent customerVerificationParallelAgent;

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Override
    public Map<String, Object> runParallel(String customerId) throws GraphRunnerException {
        OverAllState overAllState = customerVerificationParallelAgent.invoke(Map.of("customerId", customerId)).orElse(null);
        if (overAllState == null || CollUtil.isEmpty(overAllState.data())) {
            return Map.of("status", "fail", "customerId", customerId, "msg", "代理未返回结果");
        }

        Map<String, Object> verificationResultMap = new HashMap<>();
        Map<String, Object> dataMap = overAllState.data();

        // 从状态中提取各个代理结果
        verificationResultMap.put("creditScoreResult", parseMetricResult(multiAgentBuild.extractText(dataMap, "creditScoreResult")));
        verificationResultMap.put("orderSuccessRateResult", parseMetricResult(multiAgentBuild.extractText(dataMap, "orderSuccessRateResult")));
        verificationResultMap.put("orderAveragePriceResult", parseMetricResult(multiAgentBuild.extractText(dataMap, "orderAveragePriceResult")));
        verificationResultMap.put("refundRateCheckResult", parseMetricResult(multiAgentBuild.extractText(dataMap, "refundRateCheckResult")));

        // 计算综合评估
        String overallAssessment = calculateOverallAssessment(verificationResultMap);
        verificationResultMap.put("overallAssessment", overallAssessment);
        verificationResultMap.put("customerId", customerId);
        verificationResultMap.put("status", "success");
        return verificationResultMap;
    }

    /**
     * 解析指标结果
     */
    private Map<String, String> parseMetricResult(String resultText) {
        if (StrUtil.isBlank(resultText)) {
            return Map.of("值", "N/A", "评估", "无法解析");
        }

        Map<String, String> resultMap = new HashMap<>();
        String[] parts = resultText.split("，");

        for (String part : parts) {
            String[] keyValue = part.split("：");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // 提取数值
                if (key.contains("分") || key.contains("率") || key.contains("价")) {
                    // 使用正则表达式提取数值
                    Pattern pattern = Pattern.compile("([0-9]+\\.?[0-9]*)[%元]?");
                    Matcher matcher = pattern.matcher(value);
                    if (matcher.find()) {
                        resultMap.put("数值", matcher.group(1));
                    }
                }

                resultMap.put(key, value);
            }
        }

        return resultMap;
    }

    /**
     * 计算综合评估
     */
    private String calculateOverallAssessment(Map<String, Object> results) {
        int score = 0;
        int maxScore = 16; // 4个维度，每个维度最高4分

        // 检查每个维度的结果
        for (Map.Entry<String, Object> entry : results.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, String> metricResult = (Map<String, String>) entry.getValue();
                String assessment = metricResult.get("评估等级");
                if (StrUtil.isNotBlank(assessment)) {
                    if (assessment.contains("优秀") || assessment.contains("高价值") || assessment.contains("低风险")) {
                        score += 4;
                    } else if (assessment.contains("良好") || assessment.contains("中等价值") || assessment.contains("中等风险")) {
                        score += 3;
                    } else if (assessment.contains("一般")) {
                        score += 2;
                    } else if (assessment.contains("较差") || assessment.contains("低价值") || assessment.contains("高风险")) {
                        score += 1;
                    }
                }
            }
        }

        // 计算百分比
        double percentage = (double) score / maxScore * 100;
        if (percentage >= 80) {
            return String.format("综合评分：%.1f%% - 优质客户，建议重点维护", percentage);
        } else if (percentage >= 60) {
            return String.format("综合评分：%.1f%% - 普通客户，可正常服务", percentage);
        } else {
            return String.format("综合评分：%.1f%% - 需关注客户，建议加强沟通", percentage);
        }
    }

}
