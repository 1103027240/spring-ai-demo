package cn.example.base.demo.service.impl;

import cn.example.base.demo.dto.DocumentReviewDto;
import cn.example.base.demo.dto.DocumentReviewResumeDto;
import cn.example.base.demo.enums.ApprovalDecisionEnum;
import cn.example.base.demo.enums.ApprovalStatusEnum;
import cn.example.base.demo.service.DocumentReviewService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static cn.example.base.demo.enums.DocumentReviewNodeEnum.HUMAN_APPROVAL;

/**
 * @author 11030
 */
@Slf4j
@Service
public class DocumentReviewServiceImpl implements DocumentReviewService {

    @Resource(name = "documentReviewGraph")
    private CompiledGraph documentReviewGraph;

    private final Map<String, String> executionResults = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> startWorkflow(DocumentReviewDto dto) {
        // 准备初始输入数据
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("document_content", dto.getDocumentContent());
        inputs.put("approval_decision", ApprovalDecisionEnum.PENDING.getId()); // 初始化决策状态为审批中
        inputs.put("approval_status", ApprovalStatusEnum.PENDING.getId()); // 初始化流程状态为审批中

        // 创建运行配置，将instanceId设置为threadId，threadId用于MysqlSaver查找工作流实例状态
        String instanceId = UUID.randomUUID().toString();
        RunnableConfig config = RunnableConfig.builder().threadId(instanceId).build();

        try {
            // 1.执行工作流，直至遇到配置的中断点（人工审批前），此时，当前完整状态会通过MysqlSaver自动保存到数据库
            Flux<NodeOutput> outputFlux = documentReviewGraph.stream(inputs, config);

            // 2.订阅结果（异步处理）
            outputFlux.subscribe(
                    output -> handleNodeOutput(output, instanceId),
                    error -> log.error("工作流执行失败: instanceId = {}，异常信息：{}", instanceId, error),
                    () -> log.info("工作流执行完成（非中断情况）: instanceId = {}", instanceId));

            // 由于中断是异步发生的，这里立即返回。客户端应通过/state接口轮询状态，或等待/webhook通知。
            log.info("1.工作流实例[{}]已启动。它将在人工审批节点前中断，等待您的决策。\n" +
                    "2.人工审批节点前中断后。调用/resume接口，传入approval_decision以继续流程。", instanceId);

            return Map.of("success", true, "instanceId", instanceId);
        } catch (Exception e) {
            throw new RuntimeException("启动工作流异常", e);
        }
    }

    @Override
    public Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto) {
        String instanceId = dto.getInstanceId();

        try {
            // 创建运行配置
            RunnableConfig interruptionConfig = RunnableConfig.builder()
                    .threadId(instanceId)
                    .build();

            // 1.获取当前状态快照数据
            StateSnapshot stateSnapshot = documentReviewGraph.stateOf(interruptionConfig).orElse(null);
            if (Objects.isNull(stateSnapshot)) {
                return Map.of(
                        "success", false,
                        "instanceId", instanceId,
                        "error", String.format("未找到工作流实例[%s]对应的检查点。可能执行已完成、不存在或未配置检查点保存器(BaseCheckpointSaver)。", instanceId));
            }
            log.info("成功获取当前状态快照: nodeId = {}, nextNode = {}, checkPointId = {}",
                    stateSnapshot.node(), stateSnapshot.next(), stateSnapshot.config().checkPointId().orElse("N/A"));

            // 判断中断点是否是人工审批节点
            if(!HUMAN_APPROVAL.getText().equals(stateSnapshot.next())){
                return Map.of(
                        "success", false,
                        "instanceId", instanceId,
                        "error", String.format("该工作流实例[%s]对应的当前节点[%s]，不是中断节点[%s]", instanceId, stateSnapshot.next(), HUMAN_APPROVAL.getText()));
            }

            // 准备恢复数据
            Map<String, Object> reviewUpdates = new HashMap<>();
            reviewUpdates.put("approval_decision", dto.getDecision());
            reviewUpdates.put("approver_comment", dto.getComment());
            reviewUpdates.put("approver", dto.getApprover());

            // 2.更新状态数据
            RunnableConfig resumeConfig = documentReviewGraph.updateState(interruptionConfig, reviewUpdates);

            // 3.重新获取当前状态数据（更新后的状态数据）
            Map<String, Object> resumeStateMap  = documentReviewGraph.getState(resumeConfig).state().data();

            // 4.执行工作流
            Flux<NodeOutput> resumeFlux = documentReviewGraph.stream(resumeStateMap, resumeConfig);

            // 5.订阅处理
            resumeFlux.subscribe(
                    output -> handleNodeOutput(output, instanceId),
                    error -> log.error("工作流恢复执行失败: thread_id = {}，异常信息：{}", instanceId, error),
                    () -> log.info("工作流恢复执行完成: thread_id = {}", instanceId));

            // 节点中断恢复开始执行
            log.info("工作流实例[{}]中断节点已找到，恢复执行已启动。当前节点：{}，下一个节点：{}",
                    instanceId, stateSnapshot.node(), stateSnapshot.next());

            return Map.of(
                    "success", true,
                    "instanceId", instanceId,
                    "finalReport", executionResults.get(instanceId));
        } catch (Exception e) {
            throw new RuntimeException("工作流恢复过程失败", e);
        }
    }

    /**
     * 处理节点输出
     */
    private void handleNodeOutput(NodeOutput output, String instanceId) {
        // 获取节点ID
        String nodeId = output.node();

        // 获取关联的智能体名称（可能为null）
        String agentName = output.agent();

        // 获取该节点的完整状态
        OverAllState stateAtNode = output.state();

        // 1. 记录节点执行信息
        log.info("工作流实例 [{}] 节点执行: 节点ID = {}, 代理 = {}, 是否开始节点 = {}, 是否结束节点 = {}, 状态大小 = {}",
                instanceId,
                nodeId,
                agentName != null ? agentName : "N/A",
                output.isSTART(),
                output.isEND(),
                stateAtNode.data().size());

        // 调试：打印状态中的关键字段
        Map<String, Object> stateData = stateAtNode.data();

        // 2. 检查是否为工作流结束
        if (output.isEND()) {
            log.info("工作流实例 [{}] 已执行完成", instanceId);

            // 从最终状态中提取结果
            String finalResult = (String) stateAtNode.value("final_report").orElse("工作流执行完成");
            executionResults.put(instanceId, finalResult);
        }

        // 3. 特别处理中断情况，中断信息可能通过其他方式传递，这里我们通过检查状态中的特定字段来判断
        if (stateAtNode.value("approval_status", "").equals(ApprovalStatusEnum.WAITING.getId())) {
            log.info("=== 工作流实例 [{}] 处于待审批状态 ===", instanceId);
        }
    }

}
