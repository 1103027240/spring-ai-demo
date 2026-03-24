package cn.example.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 拒绝处理节点
 * @author 11030
 */
@Component
@Slf4j
public class RejectProcessingNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) {
            log.info("执行拒绝处理逻辑...");
            return Map.of("processing_result", "文档已拒绝，已通知相关人员");
    }

}
