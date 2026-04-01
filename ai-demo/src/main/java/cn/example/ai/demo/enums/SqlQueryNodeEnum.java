package cn.example.ai.demo.enums;

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

    WORKFLOW_ID_GENERATE_NODE("workflowIdGenerateNode", "流程ID生成"),

    NL_TO_SQL_NODE("nlToSqlNode", "自然语言查询转SQL"),

    VALIDATE_SQL_NODE("validateSqlNode", "SQL验证"),

    EXECUTE_SQL_NODE("executeSqlNode", "SQL执行"),

    ANALYSIS_NODE("analysisNode", "数据分析"),

    QUERY_RESULT_GENERATE("queryResultGenerateNode", "查询结果生成"),

    ERROR_HANDLE_NODE("errorHandleNode", "错误处理"),

    ;

    private String id;

    private String text;

}
