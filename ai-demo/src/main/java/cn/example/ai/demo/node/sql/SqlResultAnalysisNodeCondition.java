package cn.example.ai.demo.node.sql;

import cn.example.ai.demo.build.MultiAgentBuild;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import static cn.example.ai.demo.constant.FieldConstant.*;
import static cn.example.ai.demo.enums.SqlQueryNodeEnum.QUERY_RESULT_GENERATE;

/**
 * 数据分析节点条件
 */
@Slf4j
@Component
public class SqlResultAnalysisNodeCondition implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        try {
            MultiAgentBuild multiAgentBuild = SpringUtil.getBean(MultiAgentBuild.class);
            Object result = state.value(ANALYSIS_RESULT).orElse(null);
            if (result == null) {
                log.warn("【数据分析智能体节点条件】无输出结果");
                return QUERY_RESULT_GENERATE.getText();
            }

            Map<String, Object> agentResult = multiAgentBuild.parseMap(multiAgentBuild.extractText(result));
            boolean success = (boolean) agentResult.getOrDefault(SUCCESS, false);
            if (!success) {
                log.warn("【数据分析智能体节点条件】分析数据处理失败: {}", agentResult.get(ERROR));
                // 仍然继续，因为可能有简单分析
            }

            return QUERY_RESULT_GENERATE.getText();
        } catch (Exception e) {
            log.error("【数据分析智能体节点条件】分析数据处理失败", e);

            // 仍然继续，因为可能有简单分析
            return QUERY_RESULT_GENERATE.getText();
        }
    }

}
