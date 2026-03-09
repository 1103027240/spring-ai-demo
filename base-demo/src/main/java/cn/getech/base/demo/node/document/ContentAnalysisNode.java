package cn.getech.base.demo.node.document;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 内容分析节点
 * @author 11030
 */
@Component
@Slf4j
public class ContentAnalysisNode implements NodeActionWithConfig {

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String content = (String) state.value("document_content")
                .orElseThrow(() -> new IllegalStateException("文档内容为空"));

        // 模拟内容分析
        String analysisResult = "内容分析完成：文档主题明确，结构清晰";

        return Map.of(
                "content_analysis_result", analysisResult,
                "next_node", "compliance_check");
    }

}
