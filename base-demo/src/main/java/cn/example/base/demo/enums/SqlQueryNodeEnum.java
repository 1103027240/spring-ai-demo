package cn.example.base.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 11030
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SqlQueryNodeEnum {

    WORKFLOW_ID_GENERATE_NODE("workflowIdGenerateNode", "流程ID生成节点"),

    NL_TO_SQL_NODE("nlToSqlNode", "自然语言查询转SQL节点"),

    VALIDATE_SQL_NODE("validateSqlNode", "SQL验证节点"),

    EXECUTE_SQL_NODE("executeSqlNode", "SQL执行节点"),

    ANALYSIS_NODE("analysisNode", "数据分析节点"),

    QUERY_RESULT_GENERATE("queryResultGenerateNode", "查询结果生成节点"),

    ERROR_HANDLE_NODE("errorHandleNode", "错误处理节点"),

    ;

    private String id;

    private String text;

}
