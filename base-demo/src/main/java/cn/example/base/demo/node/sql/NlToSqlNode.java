package cn.example.base.demo.node.sql;

import cn.example.base.demo.agent.QueryAgent;
import cn.example.base.demo.build.MultiAgentBuild;
import cn.example.base.demo.enums.QueryWorkflowStatusEnum;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import static cn.example.base.demo.constant.FieldConstant.*;
import static cn.example.base.demo.enums.SqlQueryNodeEnum.*;

/**
 * 2、自然语言查询转SQL节点
 */
@Slf4j
@Component
public class NlToSqlNode implements NodeAction {

    @Autowired
    private QueryAgent queryAgent;

    @Autowired
    private MultiAgentBuild multiAgentBuild;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("【数据查询智能体】自然语言查询转SQL节点开始执行");

        try {
            // 校验请求参数
            String naturalLanguageQuery = state.value(NATURAL_LANGUAGE_QUERY, String.class).orElse("");
            if (StrUtil.isBlank(naturalLanguageQuery)) {
                return Map.of(
                        ERROR, "【转SQL节点】自然语言查询为空",
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            // 通过大模型调用工具
            String agentResponse = queryAgent.call(
                    Msg.builder()
                            .role(MsgRole.USER)
                            .textContent(naturalLanguageQuery)
                            .build())
                    .getTextContent();

            Map<String, Object> agentResult = multiAgentBuild.parseJsonResponse(agentResponse);

            // 解析大模型工具返回结果
            if (!(boolean) agentResult.getOrDefault(SUCCESS, false)) {
                return Map.of(
                        NL_TO_SQL_RESULT, agentResult,
                        ERROR, "【转SQL节点】自然语言查询转SQL处理失败: " + agentResult.get(ERROR),
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            String generatedSql = (String) agentResult.getOrDefault(GENERATED_SQL, "");
            if (StrUtil.isBlank(generatedSql) || StrUtil.isBlank(generatedSql.trim())) {
                return Map.of(
                        NL_TO_SQL_RESULT, agentResult,
                        ERROR, "【转SQL节点】生成的SQL为空",
                        WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                        NEXT_NODE, ERROR_HANDLE_NODE.getId());
            }

            return Map.of(
                    NL_TO_SQL_RESULT, agentResult,
                    GENERATED_SQL, generatedSql,
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.PROCESSING.getId(),
                    CURRENT_NODE, NL_TO_SQL_NODE.getId(),
                    NEXT_NODE, VALIDATE_SQL_NODE.getId());
        } catch (Exception e) {
            log.error("【数据查询智能体】自然语言查询转SQL节点执行失败", e);
            return Map.of(
                    ERROR, e.getMessage(),
                    WORKFLOW_STATUS, QueryWorkflowStatusEnum.ERROR.getId(),
                    NEXT_NODE, ERROR_HANDLE_NODE.getId());
        }
    }

}
