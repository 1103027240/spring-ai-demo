package cn.example.ai.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 批准处理节点
 * @author 11030
 */
@Component
@Slf4j
public class ApproveProcessingNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) {
            log.info("执行批准处理逻辑...");
            return Map.of("processing_result", "文档已批准");
    }

}
