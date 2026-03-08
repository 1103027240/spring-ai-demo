package cn.getech.base.demo.service.impl;

import cn.getech.base.demo.service.EmailService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 11030
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Resource(name = "emailGraph")
    private CompiledGraph emailGraph;

    // ... existing code ...

    @Override
    public String sendEmail(String msg) throws Exception {
        log.info("=== 测试紧急账单问题 ===");

        // 测试紧急账单问题
        Map<String, Object> initialState = Map.of(
                "email_content", "我的订阅被收费两次了！这很紧急！",
                "sender_email", "customer@example.com",
                "email_id", "email_123",
                "messages", new ArrayList<String>()
        );

        // 使用 thread_id 运行以实现持久化
        var config = RunnableConfig.builder()
                .threadId("customer_123")
                .build();

        log.info("=== 开始执行工作流（将在 human_review 前中断）===");

        // 使用 stream 执行，直到中断点（human_review）
        // 图将在 human_review 处暂停（因为配置了 interruptBefore）
        Flux<NodeOutput> stream = emailGraph.stream(initialState, config);

        // 收集中断前的所有输出
        List<NodeOutput> outputs = stream.collectList().block();

        if (outputs != null) {
            log.info("=== 第一阶段执行完成，共 {} 个节点输出 ===", outputs.size());
            outputs.forEach(output -> {
                log.info("节点输出：node={}, type={}", output.node(), output.getClass().getSimpleName());
                log.info("  状态数据：{}", output.state().data());
            });
        }

        // 获取当前状态，检查是否在中断点暂停
        var currentState = emailGraph.getState(config);
        log.info("=== 当前状态 ===");
        log.info("当前状态数据：{}", currentState.state().data());

        // 检查下一个要执行的节点（关键！）
        String nextNode = (String) currentState.state().data().get("__next_node__");
        log.info("下一个要执行的节点：{}", nextNode);

        // 如果是中断状态，nextNode 应该是 human_review
        if ("human_review".equals(nextNode)) {
            log.info("✓ 成功在 human_review 节点前中断！等待人工审核...");
        } else {
            log.error("✗ 未在 human_review 节点前中断！下一个节点是：{}", nextNode);
            log.error("这意味着 interruptBefore 配置没有生效！");
        }

        // 检查是否有草稿回复
        String draftResponse = (String) currentState.state().data().get("draft_response");
        if (draftResponse != null) {
            log.info("Draft ready for review: {}...",
                    draftResponse.length() > 100
                            ? draftResponse.substring(0, 100)
                            : draftResponse);
        } else {
            log.warn("未找到 draft_response，可能流程未到达预期节点");
        }

        log.info("=== 准备提供人工审核输入 ===");

        // 准备好后，提供人工输入以恢复
        // 使用 updateState 更新状态（interruptBefore 模式下，传入 null 作为节点 ID）
        var updatedConfig = emailGraph.updateState(config, Map.of(
                "approved", true,
                "edited_response", "我们对重复收费深表歉意。我已经立即启动了退款..."
        ), null);

        log.info("=== 继续执行工作流 ===");

        // 继续执行（input 为 null，使用之前的状态）
        emailGraph.stream(null, updatedConfig)
                .doOnNext(output -> log.info("节点输出：{}", output))
                .doOnError(error -> log.error("执行错误：{}", error.getMessage()))
                .doOnComplete(() -> log.info("流完成"))
                .blockLast();

        // 获取最终状态
        var finalState = emailGraph.getState(updatedConfig);
        String status = (String) finalState.state().data().get("status");
        String draft_response = (String) finalState.state().data().get("draft_response");

        log.info("=== 工作流执行完成 ===");
        log.info("最终状态：{}", status);
        log.info("最终回复：{}", draft_response);

        return draft_response;
    }

// ... existing code ...



}
