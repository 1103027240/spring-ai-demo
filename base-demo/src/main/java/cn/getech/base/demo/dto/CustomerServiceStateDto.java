package cn.getech.base.demo.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.getech.base.demo.constant.FieldConstant.RESULT;
import static cn.getech.base.demo.constant.FieldConstant.RESULTS;
import static cn.getech.base.demo.enums.SentimentAnalysisEnum.NEGATIVE;
import static cn.getech.base.demo.enums.SentimentAnalysisEnum.URGENT;

/**
 * @author 11030
 */
@Slf4j
@Data
public class CustomerServiceStateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工作流执行ID
     */
    private String executionId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名词
     */
    private String userName;

    /**
     * 用户输入
     */
    private String userInput;

    /**
     * 意图识别结果
     */
    private String intent;

    /**
     * 情感分析结果
     */
    private String sentiment;

    /**
     * 知识库上下文
     */
    private String knowledgeContext;

    /**
     * 订单查询结果
     */
    private List<Map<String, Object>> orderResults;

    /**
     * 售后处理结果
     */
    private Map<String, Object> afterSalesResult;

    /**
     * AI回复
     */
    private String aiResponse;

    /**
     * 元数据
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 节点执行结果存储
     */
    private Map<String, Object> nodeResults = new ConcurrentHashMap<>();

    /**
     * 执行路径
     */
    private List<String> executionPath = new ArrayList<>();

    /**
     * 执行状态
     */
    private String status = "RUNNING";

    private String errorMessage;

    /**
     * 时间戳
     */
    private Long startTime;

    private Long endTime;

    public CustomerServiceStateDto() {
        this.startTime = System.currentTimeMillis();
        this.executionId = generateExecutionId();
    }

    public CustomerServiceStateDto(String sessionId, String userInput, Long userId, String userName) {
        this();
        this.sessionId = sessionId;
        this.userInput = userInput;
        this.userId = userId;
        this.userName = userName;
    }

    public CustomerServiceStateDto(String executionId, String sessionId, String userInput, Long userId, String userName, Long startTime) {
        this(sessionId, userInput, userId, userName);
        this.executionId = executionId;
        this.startTime = startTime;
    }

    private String generateExecutionId() {
        return "wf_" + UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
    }

    /**
     * 记录节点执行结果
     */
    public void recordNodeResult(String nodeName, Map<String, Object> result) {
        nodeResults.put(nodeName, result);
        executionPath.add(nodeName);

        if(result.containsKey(RESULTS)){
            this.orderResults = (List<Map<String, Object>>) result.get(RESULTS);
        }

        if(result.containsKey(RESULT)){
            this.afterSalesResult = (Map<String, Object>) result.get(RESULT);
        }
    }

    /**
     * 获取节点执行结果
     */
    public <T> T getNodeResult(String nodeName, String key, Class<T> clazz) {
        Map<String, Object> result = (Map<String, Object>) nodeResults.get(nodeName);
        if (result != null && result.containsKey(key)) {
            Object value = result.get(key);
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
        }
        return null;
    }

    /**
     * 获取所有节点结果
     */
    public Map<String, Object> getAllNodeResults() {
        return Collections.unmodifiableMap(nodeResults);
    }

    /**
     * 完成工作流
     */
    public void complete(String status, String aiResponse) {
        this.status = status;
        this.aiResponse = aiResponse;
        this.endTime = System.currentTimeMillis();
    }

    /**
     * 获取执行时长
     */
    public Long getDuration() {
        if (startTime == null) {
            return 0L;
        }
        Long end = endTime != null ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("executionId", executionId);
        map.put("sessionId", sessionId);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("userInput", userInput);
        map.put("intent", intent);
        map.put("sentiment", sentiment);
        map.put("knowledgeContext", knowledgeContext);
        map.put("orderResults", orderResults);
        map.put("afterSalesResult", afterSalesResult);
        map.put("aiResponse", aiResponse);
        map.put("metadata", new HashMap<>(metadata));
        map.put("nodeResults", new HashMap<>(nodeResults));
        map.put("executionPath", new ArrayList<>(executionPath));
        map.put("status", status);
        map.put("errorMessage", errorMessage);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("durationMs", getDuration());
        return map;
    }

    /**
     * 是否包含订单号
     */
    public boolean hasOrderNumber() {
        if (userInput == null) {
            return false;
        }
        String userInputLower = userInput.toLowerCase();
        return userInputLower.contains("ord")
                || userInputLower.contains("订单")
                || (intent != null && intent.contains("order"));
    }

    /**
     * 是否售后相关
     */
    public boolean isAfterSalesRelated() {
        if (intent == null) {
            return false;
        }
        return intent.contains("afterSales")
                || intent.contains("return")
                || intent.contains("refund")
                || intent.contains("complaint");
    }

    /**
     * 是否为紧急情况
     */
    public boolean isUrgent() {
        return URGENT.getId().equals(sentiment)
                || (userInput != null && (userInput.contains("紧急")
                || userInput.contains("急")
                || userInput.contains("立刻")
                || userInput.contains("马上")));
    }

    /**
     * 是否为负面情感
     */
    public boolean isNegative() {
        return NEGATIVE.getId().equals(sentiment)
                || (userInput != null && (userInput.contains("不满意")
                || userInput.contains("生气")
                || userInput.contains("投诉")
                || userInput.contains("差评")));
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status) || "TIMEOUT".equals(status);
    }

    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * 获取元数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> clazz) {
        Object value = metadata.get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    /**
     * 获取执行摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("执行ID: ").append(executionId);
        summary.append(", 状态: ").append(status);
        summary.append(", 时长: ").append(getDuration()).append("ms");
        if (intent != null) {
            summary.append(", 意图: ").append(intent);
        }
        if (intent != null) {
            summary.append(", 情感: ").append(intent);
        }
        return summary.toString();
    }

}