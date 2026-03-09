package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.DocumentReviewDto;
import cn.getech.base.demo.dto.DocumentReviewResumeDto;
import cn.getech.base.demo.service.DocumentReviewService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 11030
 */
@Slf4j
@Service
public class DocumentReviewServiceImpl implements DocumentReviewService {

    @Resource(name = "documentReviewGraph")
    private CompiledGraph documentReviewGraph;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Map<String, String> executionResults = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> startWorkflow(DocumentReviewDto dto) {
        // 1、生成唯一实例ID
        String instanceId = UUID.randomUUID().toString();

        // 2、准备初始输入数据
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("document_content", dto.getDocumentContent());
        inputs.put("approval_decision", "PENDING"); // 初始化为待定

        // 3、创建运行配置，关键：将 instanceId 设置为 threadId
        // threadId 用于 MysqlSaver 标识和查找特定的工作流实例状态
        RunnableConfig config = RunnableConfig.builder().threadId(instanceId).build();

        try {
            // 4、核心执行：调用 stream() 方法启动工作流
            // 工作流会同步或异步执行，直到遇到配置的中断点（human_approval前）
            // 此时，当前完整状态会通过 MysqlSaver 自动保存到数据库
            Flux<NodeOutput> outputFlux = documentReviewGraph.stream(inputs, config);

            // 5、订阅结果（异步处理）
            outputFlux.subscribe(
                    output -> handleNodeOutput(output, instanceId),
                    error -> {
                        log.error("工作流执行失败: instanceId = {}", instanceId, error);
                        executionResults.put(instanceId, "ERROR: " + error.getMessage());
                    },
                    () -> {
                        log.info("工作流执行完成（非中断情况）: instanceId = {}", instanceId);
                        executionResults.put(instanceId, "COMPLETED");
                    }
            );

            // 注意：由于中断是异步发生的，这里立即返回。
            // 客户端应通过 /state 接口轮询状态，或等待/webhook通知。
            return Map.of(
                    "success", true,
                    "instanceId", instanceId,
                    "message", "工作流已启动。它将在人工审批节点前中断，等待您的决策。",
                    "nextAction", "下一步操作。调用 /resume 接口，传入 approval_decision 以继续流程。");
        } catch (Exception e) {
            throw new RuntimeException("启动工作流异常", e);
        }
    }

    @Override
    public Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto) {
        String instanceId = dto.getInstanceId();
        try {
            // 1.将thread_id转成thread_name
            String threadName = getThreadName(instanceId);
            if (threadName == null) {
                return Map.of(
                        "success", false,
                        "error", String.format("无法根据标识[%s]，定位到有效且未释放的工作流线程。请确认：\n" +
                                "1. 工作流已成功启动并中断。\n" +
                                "2. 提供的标识正确。", instanceId));
            }

            // 2、创建运行配置，关键：将 threadId 转成为 threadName
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(threadName) // 关键：使用从数据库查出的thread_name
                    .build();

            // 3.尝试获取状态快照
            StateSnapshot stateSnapshot = documentReviewGraph.stateOf(config).orElse(null);
            if (Objects.isNull(stateSnapshot)) {
                // 此时可能仍为空，可能是更深层次的序列化等问题
                //Collection<StateSnapshot> history = documentReviewGraph.getStateHistory(config);

                // 即使有历史，也按失败处理，因为框架行为不一致
                return Map.of(
                        "success", false,
                        "instanceId", instanceId,
                        "error", "框架状态加载异常。stateOf() 返回空，但数据库中存在检查点。可能涉及状态反序列化问题。",
                        "suggestion", "请检查应用日志中是否有 MysqlSaver 相关的反序列化错误。");
            } else {
                // 成功获取状态，进行恢复
                log.info("成功获取状态快照: nodeId={}, nextNode={}, checkPointId={}",
                        stateSnapshot.node(), stateSnapshot.next(), stateSnapshot.config().checkPointId().orElse("N/A"));

                // 4.准备恢复数据
                Map<String, Object> updates = new HashMap<>();
                updates.put("approval_decision", dto.getDecision());
                updates.put("approver_comment", dto.getComment());
                updates.put("approver", dto.getApprover());

                // 5.更新状态并恢复执行
                RunnableConfig updatedConfig = documentReviewGraph.updateState(config, updates);
                Flux<NodeOutput> resumeFlux = documentReviewGraph.stream(Map.of(), updatedConfig);

                // 6.订阅处理结果
                resumeFlux.subscribe(
                        output -> handleNodeOutput(output, instanceId),
                        error -> {
                            log.error("工作流恢复执行失败: thread_id = {}", instanceId, error);
                            executionResults.put(instanceId, "RESUME_ERROR: " + error.getMessage());
                        },
                        () -> {
                            log.info("工作流恢复执行完成: thread_id = {}", instanceId);
                            executionResults.put(instanceId, "COMPLETED");
                        }
                );

                return Map.of(
                        "success", true,
                        "instanceId", instanceId,
                        "message", "工作流状态已找到，恢复执行已启动。",
                        "currentNode", stateSnapshot.node(),
                        "nextNode", stateSnapshot.next()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("工作流恢复过程失败", e);
        }
    }

    /**
     * 处理节点输出
     */
    private void handleNodeOutput(NodeOutput output, String instanceId) {
            // 1. 获取节点ID（根据 NodeOutput 文档，node() 返回节点标识符）
            String nodeId = output.node();

            // 2. 获取关联的智能体名称（可能为null）
            String agentName = output.agent();

            // 3. 获取该节点的完整状态
            OverAllState stateAtNode = output.state();

            // 4. 记录节点执行信息
            log.info("实例 [{}] 节点执行: 节点ID = {}, 代理 = {}, 是否开始节点 = {}, 是否结束节点 = {}, 状态大小 = {}",
                    instanceId,
                    nodeId,
                    agentName != null ? agentName : "N/A",
                    output.isSTART(),
                    output.isEND(),
                    stateAtNode.data().size());

            // 5. 调试：打印状态中的关键字段
            Map<String, Object> stateData = stateAtNode.data();

            // 查找并记录审批相关的字段
            stateData.forEach((key, value) -> {
                if (key.contains("approval") || key.contains("document") || key.contains("result")) {
                    log.debug("状态字段 [{}] = {}", key,
                            value instanceof String ?
                                    ((String) value).substring(0, Math.min(50, ((String) value).length())) + "..." :
                                    value);
                }
            });

            // 6. 检查是否为工作流结束
            if (output.isEND()) {
                log.info("工作流实例 [{}] 已执行完成", instanceId);

                // 从最终状态中提取结果
                String finalResult = (String) stateAtNode.value("final_result").orElse("工作流执行完成");
                executionResults.put(instanceId, finalResult);
            }

            // 7. 特别处理中断情况
            // 注意：根据 NodeOutput 文档，没有 isInterruption() 方法
            // 中断信息可能通过其他方式传递，这里我们通过检查状态中的特定字段来判断
            if (stateAtNode.value("approval_status", "").equals("PENDING")) {
                log.warn("=== 工作流处于等待审批状态 ===");
                executionResults.put(instanceId, "INTERRUPTED - 等待人工审批");
            }
    }

    /**
     * 获取thread_name：优先匹配 thread_name，若无则匹配 thread_id
     */
    private String getThreadName(String threadId) {
        if (StrUtil.isBlank(threadId)) {
            return null;
        }

        String sql = "SELECT thread_name, is_released FROM GRAPH_THREAD " +
                "WHERE thread_name = ? OR thread_id = ? " +
                "ORDER BY thread_name = ? DESC LIMIT 1"; // 优先匹配 thread_name

        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, threadId, threadId, threadId);
            if (CollUtil.isEmpty(records)) {
                log.warn("数据库中未找到thread_name或thread_id为[{}]的记录。", threadId);
                return null;
            }

            Map<String, Object> record = records.get(0);
            Boolean isReleased = (Boolean) record.get("is_released");
            String threadName = (String) record.get("thread_name");
            if (Boolean.TRUE.equals(isReleased)) {
                log.warn("找到thread_name=[{}]，但其状态为已释放（is_released=true）。", threadName);
                return null; // 线程已释放，不可用
            }

            return threadName;
        } catch (Exception e) {
            throw new RuntimeException("解析thread_name时发生数据库异常", e);
        }
    }

}
