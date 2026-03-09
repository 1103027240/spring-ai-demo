package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.dto.DocumentReviewDto;
import cn.getech.base.demo.dto.DocumentReviewResumeDto;
import cn.getech.base.demo.service.DocumentReviewService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 11030
 */
@Slf4j
@Service
public class DocumentReviewServiceImpl implements DocumentReviewService {

    @Resource(name = "documentReviewGraph")
    private CompiledGraph documentReviewGraph;

    @Override
    public Map<String, Object> startWorkflow(DocumentReviewDto dto) {
        // 生成唯一实例ID
        String instanceId = UUID.randomUUID().toString();

        // 准备初始状态
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("document_content", dto.getDocumentContent());
        initialState.put("workflow_instance_id", instanceId);

        // 创建运行配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId(instanceId)  // 关键：使用instanceId作为threadId
                .build();

        try {
            // 执行工作流（流式执行，会在中断节点暂停）
            documentReviewGraph.stream(initialState, config)
                    .subscribe(
                            output -> handleNodeOutput(output, instanceId),
                            error -> log.error("工作流执行错误: {}", error.getMessage(), error),
                            () -> log.info("工作流执行完成: instanceId = {}", instanceId));

            return Map.of(
                    "success", true,
                    "instanceId", instanceId,
                    "message", "工作流已启动，等待执行到审批节点");
        } catch (Exception e) {
            log.error("启动工作流失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> resumeWorkflow(DocumentReviewResumeDto dto) {
        String instanceId = dto.getInstanceId();

        // 准备恢复数据（包含审批决定）
        Map<String, Object> resumeData = new HashMap<>();
        resumeData.put("approval_decision", dto.getDecision());
        resumeData.put("approval_comment", dto.getComment());
        resumeData.put("approver", dto.getApprover());

        // 创建运行配置（必须使用相同的 threadId）
        RunnableConfig config = RunnableConfig.builder()
                .threadId(instanceId)
                .build();

        try {
            // 恢复执行：再次调用 stream() 方法，传入审批决定
            // 框架会自动从检查点恢复，human_approval 节点会获取到 approval_decision
            documentReviewGraph.stream(resumeData, config)
                    .subscribe(
                            output -> handleNodeOutput(output, instanceId),
                            error -> log.error("工作流恢复错误：{}", error.getMessage(), error),
                            () -> log.info("工作流恢复完成：instanceId = {}", instanceId));

            return Map.of(
                    "success", true,
                    "instanceId", instanceId,
                    "message", "工作流已恢复，继续执行后续节点"
            );

        } catch (Exception e) {
            log.error("恢复工作流失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 处理节点输出
     */
    private void handleNodeOutput(NodeOutput output, String instanceId) {
        log.info("实例[{}]节点输出: {}", instanceId, output);

        // 可以根据节点输出类型进行不同处理
//        if (output.isInterruption()) {
//            log.info("工作流已中断，等待人工审批: instanceId = {}", instanceId);
//            // 这里可以发送通知、更新数据库状态等
//        }
    }

}
